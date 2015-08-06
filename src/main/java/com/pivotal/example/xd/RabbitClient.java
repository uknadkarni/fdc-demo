package com.pivotal.example.xd;

import java.io.IOException;
import java.util.Iterator;

import org.apache.log4j.Logger;
import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;
import org.springframework.amqp.core.AcknowledgeMode;
import org.springframework.amqp.core.AnonymousQueue;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.FanoutExchange;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageListener;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.connection.CachingConnectionFactory;
import org.springframework.amqp.rabbit.connection.Connection;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.rabbit.listener.SimpleMessageListenerContainer;
import org.springframework.cloud.Cloud;
import org.springframework.cloud.CloudException;
import org.springframework.cloud.CloudFactory;
import org.springframework.cloud.service.ServiceInfo;
import org.springframework.cloud.service.common.RabbitServiceInfo;

import com.pivotal.example.xd.controller.OrderController;
import com.rabbitmq.client.ConnectionFactory;

public class RabbitClient {

	static Logger logger = Logger.getLogger(RabbitClient.class);
	private static RabbitClient instance;
	private CachingConnectionFactory ccf;
	private Queue orderQueue;
	private Queue orderProcQueue;
	private RabbitTemplate rabbitTemplate;
	
	private static final String EXCHANGE_NAME="ORDERS_EXCHANGE";
	private static final String ORDER_PROCESSING_QUEUE = "ORDERS_QUEUE";
	private static final String ORDER_RECEIVING_QUEUE = "ORDERS_RECEIVING_QUEUE";

	Connection connection;
	private String rabbitURI;
	
	private RabbitClient(){
		
    	try{
    		Cloud cloud = new CloudFactory().getCloud();
	    	Iterator<ServiceInfo> services = cloud.getServiceInfos().iterator();
	    	while (services.hasNext()){
	    		ServiceInfo svc = services.next();
	    		if (svc instanceof RabbitServiceInfo){
	    			RabbitServiceInfo rabbitSvc = ((RabbitServiceInfo)svc);	    			
	    			rabbitURI=rabbitSvc.getUri();
	    			System.out.println("RabbitURI: " + rabbitURI);
	    			try{
	    				
	    				ConnectionFactory factory = new ConnectionFactory();
	    				factory.setUri(rabbitURI);
	    				ccf = new CachingConnectionFactory(factory);
	    				
	    				connection = ccf.createConnection();
	    
	    				FanoutExchange fanoutExchange = new FanoutExchange(EXCHANGE_NAME, false, true);
	    				
	    				RabbitAdmin rabbitAdmin = new RabbitAdmin(ccf);
	    				
	    				rabbitAdmin.declareExchange(fanoutExchange);
	    				
	    				// orderQueue = new AnonymousQueue();
	    				orderQueue = new Queue(ORDER_RECEIVING_QUEUE);
	    				rabbitAdmin.declareQueue(orderQueue);
	    				rabbitAdmin.declareBinding(BindingBuilder.bind(orderQueue).to(fanoutExchange));
	    				
	    				orderProcQueue = new Queue(ORDER_PROCESSING_QUEUE);
	    				rabbitAdmin.declareQueue(orderProcQueue);
	    				rabbitAdmin.declareBinding(BindingBuilder.bind(orderProcQueue).to(fanoutExchange));
	    				
	    				
	    				rabbitTemplate = rabbitAdmin.getRabbitTemplate();
	    				rabbitTemplate.setExchange(EXCHANGE_NAME);
	    				rabbitTemplate.setConnectionFactory(ccf);
	    				
	    				rabbitTemplate.afterPropertiesSet();
	    					    				
	    			}
	    			catch(Exception e){
	    				throw new RuntimeException("Exception connecting to RabbitMQ",e);
	    			}
	    			
	    		}
	    	}
    	}
    	catch(CloudException ce){
    		// means its not being deployed on Cloud
    		logger.warn(ce.getMessage());
    	}
		
		
	}
	
	public static synchronized RabbitClient getInstance(){
		if (instance==null){
			instance = new RabbitClient(); 
		}
		return instance;
	}
	
	public synchronized void post(Order order) throws IOException{
		
		rabbitTemplate.send(new Message(order.toBytes(), new MessageProperties()));
	}

	public void startMessageListener(){
		
		SimpleMessageListenerContainer container = new SimpleMessageListenerContainer(ccf);		
		container.setQueues(orderQueue);
		container.setMessageListener(new MessageListener() {
			
			@Override
			public void onMessage(Message message) {
				// System.out.println(message.getBody());
				// Order order = Order.fromBytes(message.getBody()); // Error here
				Order order = new Order();
				String orderStr = new String(message.getBody());
				System.out.println("Listening Order: " + orderStr);
				try {
					order = new ObjectMapper().readValue(orderStr, Order.class);
				} catch (JsonParseException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (JsonMappingException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				OrderController.registerOrder(order);
			}
		});
		container.setAcknowledgeMode(AcknowledgeMode.AUTO);
		container.start();
		
		
	}

	public void startOrderProcessing(){
		
		SimpleMessageListenerContainer container = new SimpleMessageListenerContainer(ccf);		
		container.setQueues(orderProcQueue);
		container.setMessageListener(new MessageListener() {
			
			@Override
			public void onMessage(Message message) {
				//for now simply log the order
				//Order order = Order.fromBytes(message.getBody()); //Error here
				//Order order = new Order();
				//System.out.println("Processing Order: " + order);
				//logger.info("Process Order: " + order.getState()+":"+order.getAmount());

				Order order = new Order();
				String orderStr = new String(message.getBody());
				System.out.println("Listening Order: " + orderStr);
				try {
					order = new ObjectMapper().readValue(orderStr, Order.class);
				} catch (JsonParseException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (JsonMappingException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				// String orderStr = new String(message.getBody());
				System.out.println("Processing Order: " + orderStr);
			}
		});
		container.setAcknowledgeMode(AcknowledgeMode.AUTO);
		container.start();
		
		
	}

	
	
	public boolean isBound(){
		return (rabbitURI!=null);
	}
	
	public String getRabbitURI(){
		return rabbitURI;
	}
}
