package test;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

import qos.Message;

class TestMessage {

	@Test
	void testDumpMessage() {
		Message m1 = new Message(Message.X, "hello");
		System.out.println(m1.toString()); // <<X::hello>>
		assertEquals(m1.toString(), "<<X::hello>>");
	}

	@Test
	void testParseMessage() {
		Message m1 = new Message(Message.X, "hello");
		System.out.println(Message.fromString("<<X::hello>>")); 
		assertEquals(m1.toString(), Message.fromString("<<X::hello>>").toString());
	}

}
