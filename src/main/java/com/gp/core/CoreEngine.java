package com.gp.core;

import java.util.Date;
import java.util.ServiceLoader;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.Assert;

import com.gp.exception.BaseException;
import com.gp.launcher.CoreInitializer;
import com.gp.launcher.Lifecycle;
import com.gp.launcher.Lifecycle.LifeState;
import com.gp.launcher.LifecycleListener;

/**
 * CoreFacade maintains a delegate instance which implements Lifecycle.
 * firstly it loads all the initializers from /META-INF/com.gp.launcher.CoreInitializer
 * currently includes: <br>
 * <ol>
 * <li>com.gp.disruptor.EventEngineInitializer - the event engine build on disruptor.</li>
 * </ol>
 * 
 * @author despird
 * @version 0.1 2014-12-1
 * 
 **/
public class CoreEngine{

	Logger LOGGER = LoggerFactory.getLogger(CoreEngine.class);
	
	private static CoreDelegator coreDelegator;
	
	private static CoreFacade coreFacade;
	
	// auto fire CoreDelegator initialization.
	static{
		new CoreEngine();
	}
	
	/**
	 * Hide the default constructor 
	 **/
	private CoreEngine(){
		
		if(coreDelegator == null) 
			coreDelegator = new CoreDelegator();
		
		LOGGER.debug("CoreDelegator setup runs.");
		coreDelegator.setup();
	}
	
	/**
	 * Get the core facade support delegate for methods required for core package 
	 **/
	public static CoreFacade getCoreFacade() {
		Assert.notNull(coreFacade, "the core facade must not be null.");
		return coreFacade;
	}
	
	/**
	 * Initial the core, during initial phase necessary resource and classes are loaded
	 * so some LifecycleListener will be registered in CoreDelegator instance.
	 **/
	public static void initial(CoreFacade _coreFacade) throws BaseException{
		
		coreFacade = _coreFacade;
		coreDelegator.initial();
	}
	
	/**
	 * Start core and fire ILifecycle.State.START
	 **/
	public static void startup() throws BaseException{
		
		coreDelegator.startup();
	}
	
	/**
	 * Stop core and fire ILifecycle.State.STOP
	 **/
	public static void shutdown()throws BaseException{
		
		coreDelegator.shutdown();
	}
	
	/**
	 * State of core 
	 **/
	public static LifeState state() {
		
		return coreDelegator.state();
	}

	/**
	 * Register the life cycle listener
	 * @param listener  
	 **/
	public static void regLifecycleHooker(LifecycleListener hooker) {
		
		coreDelegator.regLifecycleHooker(hooker);
	}
	
	/**
	 * Unregister the life cycle listener
	 * @param listener  
	 **/
	public static void unregLifecycleHooker(LifecycleListener hooker) {

		coreDelegator.unregLifecycleHooker(hooker);
	}

	/**
	 * Clear the life cycle listeners
	 **/
	public static void clearListener() {

		coreDelegator.clearLifecycleHooker();
	}
	
	/**
	 * Delegate class to implements the ILifecycle method support. 
	 **/
	private static class CoreDelegator extends Lifecycle{
		
		static Logger LOGGER = LoggerFactory.getLogger(CoreEngine.class);
		
		/**
		 * Default constructor 
		 **/
		public CoreDelegator(){	}
		
		/**
		 * Trigger the CoreInitializers to setup LifecycleHooker
		 * !!! IMPORTANT !!!
		 * If CoreFacade is not deployed in webapp, the recommended ClassLoader is ClassLoader.getSystemClassLoader();
		 * Here we have to change it to CoreEngine.class.getClassLoader, otherwise it cannot loaded spi file in META-INF/services.
		 * 
		 * ServiceLoader<CoreInitializer> svcloader = ServiceLoader
	     *           .load(CoreInitializer.class, ClassLoader.getSystemClassLoader());
		 * 
		 **/
		public void setup(){
		
	        ServiceLoader<CoreInitializer> svcloader = ServiceLoader
	                .load(CoreInitializer.class, CoreEngine.class.getClassLoader());
	        
	        for (CoreInitializer initializer : svcloader) {
	        	
	        		LOGGER.info("Initializer : {} is loaded.",initializer.getClass().getName());
	        		regLifecycleHooker(initializer.getLifecycleHooker());
	        }
		}
		
		@Override
		public void initial() throws BaseException{
			
			// fire up the Lifecycle event to trigger process
			fireEvent(LifeState.INITIAL);
			
			this.state = LifeState.INITIAL;
		}
		
		@Override
		public void startup() throws BaseException{
			
			state = LifeState.STARTUP;
			fireEvent(LifeState.STARTUP);
			this.state = LifeState.RUNNING;
		}
		
		@Override
		public void shutdown()throws BaseException{
			
			fireEvent(LifeState.SHUTDOWN);
			state = LifeState.SHUTDOWN;
		}
			
	}
	
	/** Inner Message class */
	protected static class LifeCycleMessage {
		Date time = null;
		String message = null;
		boolean errorFlag = false;

		public LifeCycleMessage(Date time, boolean errorFlag, String message) {
			this.time = time;
			this.errorFlag = errorFlag;
			this.message = message;
		}
	}
}
