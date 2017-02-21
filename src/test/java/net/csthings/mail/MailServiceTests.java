package net.csthings.mail;

import java.io.IOException;
import java.util.List;

import net.csthings.common.configuration.ConfigurationService;
import net.csthings.common.db.DatabaseService;
import net.csthings.common.dto.ResultDto;
import net.csthings.common.integration.InjectorIntegrationService;
import net.csthings.common.integration.Integrator;
import net.csthings.common.integration.exception.IntegrationException;
import net.csthings.db.cassandra.test.CassandraDatabaseTestInitModule;
import net.csthings.mail.utils.TestProperties;
import net.csthings.services.test.AccountIntegrationTest.TestIntegrationModuleFactory;
import net.sourceforge.htmlunit.corejs.javascript.Context;

import org.testng.annotations.Test;

import com.google.inject.Binder;
import com.google.inject.Scopes;

import cs121.hb.services.MailService;
import cs121.hb.services.impl.MailServiceImpl;
import cs121.utils.ModelKeys;

@Integrator(modules = TestIntegrationModuleFactory.class)
public class MailServiceTests extends InjectorIntegrationService{
	public DatabaseService dbService;
	public ConfigurationService configService;
	public MailService mailService;
	
	 public MailServiceTests() throws InjectorIntegrationException {
        dbService = injector.getInstance(DatabaseService.class);
        configService = injector.getInstance(ConfigurationService.class);
        mailService = new MailServiceImpl(configService, dbService);
        new TestProperties();
	}

	public static class TestIntegrationModuleFactory extends CassandraDatabaseTestInitModule {
	        public TestIntegrationModuleFactory() throws IntegrationException, IOException {
	            super();
	        }
	    }
	
	@Test
	public void testMailGeneration(){
		int count = 50000;
		ResultDto<List<String>> rez = mailService.generateEmails(count, false);
	}
	
}
