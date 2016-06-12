package testingPackage;

import static org.junit.Assert.*;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import org.junit.BeforeClass;
import org.junit.Test;

import bgu.spl.mics.impl.MessageBusImpl;

public class TestingMessageBusImpl
{
	private static TestSender testSender;
	private static TestReciever testReciever;
	private static Executor exe;
	@BeforeClass
	public static void setUp() throws Exception
	{
		testSender = new TestSender("Sender");
		testReciever = new TestReciever("Reciever");
		exe = Executors.newFixedThreadPool(2);
		exe.execute(testReciever);
		exe.execute(testSender);
	}
	
	@Test
	public void testBusSingleton()
	{
		assertNotNull(MessageBusImpl.getInstance());
	}
	@Test
	public void testRegister()
	{
		testReciever.register();
		testSender.register();
		assertEquals(MessageBusImpl.getInstance().TESTContains(testSender), true);
		assertEquals(MessageBusImpl.getInstance().TESTContains(testReciever), true);

	}
	@Test
	public void testSendingRequest() throws InterruptedException
	{
		testReciever.register();
		testSender.register();
		//MessageBusImpl.getInstance().sendRequest(new TestRequest(),null);
		Thread.sleep(1000);
		testSender.sendRequest("hello");
		Thread.sleep(1000);
		assertEquals("TestRequest", testReciever.recieve());
	}
	@Test
	public void testSendingBroadcast() throws InterruptedException
	{
		testSender.sendBroadcast("hello");
		Thread.sleep(1000);
		assertEquals("TestBroadcast", testReciever.recieve());
	}
	@Test
	public void testUnregister()
	{
		assertEquals(MessageBusImpl.getInstance().TESTContains(testSender), true);
		assertEquals(MessageBusImpl.getInstance().TESTContains(testReciever), true);
		testReciever.unregister();
		testSender.unregister();
		assertEquals(MessageBusImpl.getInstance().TESTContains(testSender), false);
		assertEquals(MessageBusImpl.getInstance().TESTContains(testReciever), false);
	}


	
}
