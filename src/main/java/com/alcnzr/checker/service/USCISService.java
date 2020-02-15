package com.alcnzr.checker.service;

import java.io.IOException;
import java.time.Duration;
import java.time.LocalDate;
import java.util.List;

import javax.annotation.PostConstruct;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import com.alcnzr.checker.model.Constants;
import com.alcnzr.checker.model.Error;
import com.alcnzr.checker.model.Event;
import com.alcnzr.checker.model.Request;
import com.alcnzr.checker.repository.DBHelper;
import com.gargoylesoftware.htmlunit.BrowserVersion;
import com.gargoylesoftware.htmlunit.FailingHttpStatusCodeException;
import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.HtmlDivision;
import com.gargoylesoftware.htmlunit.html.HtmlInput;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import com.gargoylesoftware.htmlunit.html.HtmlSubmitInput;
import com.twilio.Twilio;
import com.twilio.rest.api.v2010.account.Message;
import com.twilio.type.PhoneNumber;

import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

@Service
public class USCISService {
	
	@Autowired
	private DBHelper helper;
	
	public static int count = -1;
	private static final Logger logger = LoggerFactory.getLogger(USCISService.class);
	private WebClient client;
	
	@Value("${twilio.sid}")
	private String ACCOUNT_SID;
	@Value("${twilio.token}")
	private String AUTH_TOKEN;
	@Value("${twilio.to}")
	private String TO_PHONE;
	@Value("${twilio.from}")
	private String FROM_PHONE;
	
	@PostConstruct
	public void init() {
		Twilio.init(ACCOUNT_SID, AUTH_TOKEN);
		client = new WebClient(BrowserVersion.CHROME);
	    client.getOptions().setThrowExceptionOnScriptError(false);
	}
	
	@Scheduled(fixedDelay = 7200000)
	public void getStatuses() {
		count = count + 1;
		logger.info("Count: {}", count);
		try {
			helper.getRep(Request.class).findAll()
			.delayElements(Duration.ofMillis(3000))
			.parallel()
			.map(req -> {
				try {
					logger.info(req.toString());
					return getAsyncResult(req);
				} catch (FailingHttpStatusCodeException e) {
					logger.info("Trying again for: {}", req.getReceipt());
					saveError(e);
					return null;
				}
			})
			.subscribe(prom -> {
				prom.subscribe(req -> {
					checkStatus(req);
				});
			});
	} catch(Exception e) {
		logger.info(e.getMessage());
		saveError(e);
	}
	}
	 
	public Mono<Request> getAsyncResult(Request rec){
		Mono<HtmlPage> landing = null;
		landing = Mono.fromCallable(() -> client.getPage(Constants.USCIS_LINK));
	
		return landing
				.map(page -> {
					logger.info("OnPage: {}", page.getBaseURL().toString());
			HtmlInput receipt = (HtmlInput) page.getElementById(Constants.RECEIPT_INPUT);
			try {
				receipt.type(rec.getReceipt());
				HtmlSubmitInput submit = page.getElementByName(Constants.SUBMIT_BTN);
				return submit.click();
			} catch (IOException e1) {
				logger.info(e1.getMessage());
				saveError(e1);
				return null;
			}
			})
				.subscribeOn(Schedulers.parallel())
				.map(page -> {
					logger.info("OnPage: {}", ((HtmlPage)page).getBaseURL().toString());
					List<HtmlDivision> elements = ((HtmlPage)page).getByXPath(Constants.STATUS_DIV);
					String status = elements.get(0).getFirstElementChild().asText();
					logger.info("Status: {}, receipt: {}", status, rec);
					rec.setStatus(status);
					return rec;
				})
				.doOnError(e -> {
					saveError(e);
					logger.info("Failed for {} due to {}", rec.getReceipt(), e.getMessage());
				});
	}
	
	private void checkStatus(Request req) {
		if(!req.getStatus().contains("Case Was Received") || (count % 24 == 0 && req.getName().contains("Joseph"))) sendTwilio(req, false);
		if(count == 1) sendTwilio(req, true);
		logger.info("Status: {}", req.getStatus());
	}
	
	private void sendTwilio(Request req, boolean ex) {
		logger.info("All: {} {} {} {}", ACCOUNT_SID, AUTH_TOKEN, TO_PHONE, FROM_PHONE);
		StringBuilder result = new StringBuilder();
		result = ex ? result.append("[START] Checking status for ").append(req.getReceipt()) : 
			result.append(
				Constants.STATUS_MESSAGE.replace("_NAME", req.getName())
				.replace("_STATUS", req.getStatus())
				.replace("_RECEIPT", req.getReceipt())
				);
		Message message = Message.creator(new PhoneNumber(req.getNumber()), new PhoneNumber(FROM_PHONE), result.toString())
				.create();
		Event event = new Event();
		event.setName(req.getName());
		event.setTime(LocalDate.now());
		event.setMessage(result.toString());
		helper.getRep(Event.class).save(event)
		.subscribe(i -> {
			logger.info("Saved event for: {}", i.getName());
		});
		logger.info("Message SID: {}",message.getSid());
	}
	
	private void saveError(Throwable e) {
		Error err = new Error();
		err.setError(e.getMessage());
		err.setTime(LocalDate.now());
		helper.getRep(Error.class).save(err)
		.subscribe(i -> logger.info("Logged error: {}", i.getError()));
	}
}
