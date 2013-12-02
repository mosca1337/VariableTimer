import java.awt.Toolkit;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class VariableTimer extends Timer {
	private TimerTask task;
	private RecursiveTask previousRecursiveTask;
	private Function function;
	
	private long x;
	private List<IterativeTask> iterativeTasks;
	private long iterations;
	private long lastIteration;
	private boolean iterating = false;
	
	// Pause, resume, and cancel
	private long lastExecution;
	private long pauseTime;
	private long deltaTime;
	private boolean isPaused = false;
	private boolean cancel = false;
	
	public static void main(String[] args) {
		
	    long currentTime = System.currentTimeMillis();
	    currentTime /= 1000;
    	System.out.println(currentTime+" start time");

		VariableTimer myTimer = new VariableTimer();
		myTimer.scheduleAtFixedVariableRate(new TimerTask() {
			public void run() {
	    	    long currentTime = System.currentTimeMillis();
	    	    currentTime /= 1000;
	        	System.out.println(currentTime);
//	        	Toolkit.getDefaultToolkit().beep();
	        }
		}, new Function() {
			public long function(long x) {
	    		return 1000*x + 1000;
	    	}
		}, 8);
		
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
    		iterations--;
    		task.run();

        	// Calculate the next period
    		x++;
    		long y = function.function(x);

    		// Must wait at least one millisecond
    		if (y > 0 && iterations != 0 && !cancel) {
    			previousRecursiveTask = new RecursiveTask(task, function);
               schedule(previousRecursiveTask, y);
    		}
        }
    }
	
    private class IterativeTask extends TimerTask {
    	private TimerTask task;
    	private long iteration;
    	
    	public IterativeTask(TimerTask task, long iteration) {
    		this.task = task;
    		this.iteration = iteration;
    	}
    	
        @Override
        public void run() {
    		// Make sure the timer hasn't been cancelled
    		if (!cancel) {
    			lastIteration = iteration;
        		lastExecution = System.currentTimeMillis();
    			task.run();
    		}
        }
    }
	
	public void scheduleAtVariableRate(TimerTask task, Function function) {
		scheduleAtVariableRate(task, function, -1);
	}
    
	// TODO
    public void scheduleAtVariableRate(TimerTask task, Function function, int iterations) {
		this.task = task;
		this.function = function;
		this.iterations = iterations;
				
		// Calculate period for positive integers
		x = 0;
		long y = function.function(x);
		
		// Execute task repeatedly until there is a period
		while (y <= 0 && iterations != 0) {
			task.run();
			x++;
			y = function.function(x);
			lastExecution = System.currentTimeMillis();
			iterations--;
		}

		// Schedule the recursion
		previousRecursiveTask = new RecursiveTask(task, function);
		schedule(previousRecursiveTask, y);
    }
    
    // TODO
    public void scheduleAtFixedVariableRate(TimerTask task, Function function, int iterations) {
    	// There should be at least one iterations
    	if (iterations < 1) {
    		return;
    	}
    	
		this.task = task;
		this.function = function;
		this.iterations = iterations;
		this.iterating = true;
		this.iterativeTasks = new ArrayList();
		
		// Calculate period for positive integers
		x = 0;
		long y = function.function(x);
		
		// Execute task repeatedly until there is a period
		while (y <= 0 && iterations != 0) {
			task.run();
			x++;
			y = function.function(x);
			lastExecution = System.currentTimeMillis();
			iterations--;
		}
		
		// Schedule all iterative tasks
		long totalTime = y;
		while (iterations != 0) {
			IterativeTask iterativeTask = new IterativeTask(task, x);
			iterativeTasks.add(iterativeTask);
			schedule(iterativeTask, totalTime);
			x++;
			y = function.function(x);
			totalTime += y;
			iterations--;
		}
    }
	
	public void cancel() {
		// Stops any runaway recursion
		cancel = true;
		super.cancel();
	}
	
	public void pause() {
		isPaused = true;
		pauseTime = System.currentTimeMillis();
		deltaTime = pauseTime - lastExecution;
		
		// Are we using iteration or recursion
		if (iterating) {
			for (IterativeTask iteration : iterativeTasks) {
				iteration.cancel();
			}
		} else {
			previousRecursiveTask.cancel();
		}
	}
	
	public void resume() {
		if (isPaused) {
			isPaused = false;
			
			// Are we using iteration or recursion
			if (iterating) {
				// TODO:
			} else {
				// Schedule the recursion
	    		long y = function.function(x);
				schedule(new RecursiveTask(task, function), (y-deltaTime));
			}
		}
	}
}