package net.csthings.mail;

import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.Assert;
import org.testng.annotations.Test;

import cs121.hb.services.utils.EmailGenerator;

@Test
public class FunctionalTests {
	
	Logger LOG = LoggerFactory.getLogger(FunctionalTests.class);

	/**
	 * Generate random emails.
	 * Makes sure none of them are equal
	 */
	public void randomEmailGeneration(){
		int numEmails = 3000;
		Set<String> emails = EmailGenerator.generateEmails(numEmails, "test.com");
		LOG.info("Generated the following emails: \n " + emails.toString());
		Assert.assertEquals(numEmails, emails.size());
	}
}
