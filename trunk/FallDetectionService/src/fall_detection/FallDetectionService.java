package fall_detection;

/**
 * Interface for the fall detection service.
 * @author Ryan Tew
 * @author John George
 *
 */
public interface FallDetectionService
{
	/**
	 * Returns true if a fall has been detected
	 * @return True if a fall has been detected
	 */
	public boolean isFallDetected();
	
	/**
	 * Gets the array of sensor values
	 * @return The array of sensor values
	 */
	public double[][] getGrid();
}
