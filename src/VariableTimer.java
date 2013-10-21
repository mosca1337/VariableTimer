import java.awt.Toolkit;
import java.util.Timer;
import java.util.TimerTask;

public class VariableTimer extends Timer {
	private TimerTask task;
	private RecursiveTask lastRecursiveTask;
	private Function function;
	private long x;
	
	private long lastExecution;
	private long pauseTime;
	private long deltaTime;
	private boolean isPaused = false;
	
	public static void main(String[] args) {

	    class PrintTask extends TimerTask {
	        @Override
	        public void run() {
	    	    long currentTime = System.currentTimeMillis();
	    	    currentTime /= 1000;
	        	System.out.println(currentTime);
	        	Toolkit.getDefaultToolkit().beep();
	        }
	    }

	    class MyFunction implements Function {
	    	public long function(long x) {
	    		return 1000*x + 1000;
	    	}
	    }
		
	    long currentTime = System.currentTimeMillis();
	    currentTime /= 1000;
    	System.out.println(currentTime+" start time");

		VariableTimer myTimer = new VariableTimer();
		myTimer.scheduleAtVariableRate(new PrintTask(), new MyFunction());
		
		// Pause after x seconds
	    class PauseTask extends TimerTask {
	    	VariableTimer timer;
	    	public PauseTask(VariableTimer timer) {
	    		this.timer = timer;
	    	}
	        @Override
	        public void run() {
	    	    timer.pause();
	        }
	    }
		Timer pauseTimer = new Timer();
//		pauseTimer.schedule(new PauseTask(myTimer), 5000);
		
		// Resume after x seconds
	    class ResumeTask extends TimerTask {
	    	VariableTimer timer;
	    	public ResumeTask(VariableTimer timer) {
	    		this.timer = timer;
	    	}
	        @Override
	        public void run() {
	    	    timer.resume();
	        }
	    }
		Timer resumeTimer = new Timer();
//		resumeTimer.schedule(new ResumeTask(myTimer), 8000);
	}
	
    private class RecursiveTask extends TimerTask {
    	private TimerTask task;
    	private Function function;
    	
    	public RecursiveTask(TimerTask task, Function function) {
    		this.task = task;
    		this.function = function;
    	}
    	
        @Override
        public void run() {
        	// Run the original task
    		lastExecution = System.currentTimeMillis();
    		task.run();
        	
        	// Calculate the next period
    		x++;
    		long y = function.function(x);

    		// Must wait at least one millisecond
    		if (y > 0) {
    			lastRecursiveTask = new RecursiveTask(task, function);
               schedule(lastRecursiveTask, y);
    		}
        }
    }
	
	public void scheduleAtVariableRate(TimerTask task, Function function) {
		this.task = task;
		this.function = function;
				
		// Calculate period for positive integers
		x = 0;
		long y = function.function(x);
		
		// Execute task if there is no period
		while (y <= 0) {
			x++;
			y = function.function(x);
			lastExecution = System.currentTimeMillis();
			task.run();
		}

		// Schedule the recursion
		lastRecursiveTask = new RecursiveTask(task, function);
		this.schedule(lastRecursiveTask, y);
	}

	public void pause() {
		isPaused = true;
		pauseTime = System.currentTimeMillis();
		deltaTime = pauseTime - lastExecution;
		lastRecursiveTask.cancel();
	}
	
	public void resume() {
		if (isPaused) {
			isPaused = false;
			// Schedule the recursion after deltaTime
			this.schedule(new RecursiveTask(task, function), deltaTime);
		}
	}
}