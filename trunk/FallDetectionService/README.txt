Process for attaching the cancel button and grid to the phidget interface kits:
The interface kits are zero indexed in the FallDetection class by their serial number,
The zeroth interface kit needs to have the cancel button plugged into its zeroth port.
From there the floor tiles need to be plugged in.
The tiles are in a zero indexed 2 dimensional grid (orientation and size does not matter),
going in row-major order plug each tile into a port on the interface kit, increasing the port number on the interface kit.
When one interface kit is full, move on to the next interface kit

The conversion from Interface Kit port to two dimensional coordinates is done in the class PressureSensorListener which is an inner class of FallDetection

The grid dimensions can be changed at the top of the FallDetection class, they are private static final constants called SENSOR_GRID_COLUMNS and SENSOR_GRID_ROWS

for a 5x3 grid of tiles the interface kits should look like this:

	zeroth IFK
	_________________________________
	|_0_|_1_|_2_|_3_|_4_|_5_|_6_|_7_|
	  C  0,0 0,1 0,2 0,3 0,4 1,0 1,1
	  A
	  N
	  C
	  E
	  L

	first IFK
	_________________________________
	|_0_|_1_|_2_|_3_|_4_|_5_|_6_|_7_|
	 1,2 1,3 1,4 2,0 2,1 2,2 2,3 2,4


Improvements we would make:

Improvements to the UI
	-automatically add domain for phone numbers

Fall Detection
	-more sophisticated fall detection algorithm (with better sensors), taking into account ammount of pressure, time pressure was applied in, etc. instead of binary sensor activated or not activated.
	-have the service load ifk serial numbers from configuration file, along with other fall detection constants