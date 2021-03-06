package mobile.team4.game;

import java.util.ArrayList;

import mobile.team4.game.GameObject.Type;
import mobile.team4.game.GameState.Mode;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.util.Log;
import android.view.GestureDetector;
import android.view.GestureDetector.OnDoubleTapListener;
import android.view.GestureDetector.OnGestureListener;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;

public class Game extends SurfaceView implements SurfaceHolder.Callback, OnGestureListener,
	OnDoubleTapListener {
	
	GameLoopThread _thread;
	Boolean isRunning;
	Boolean isPaused;
	float deltaTime;
	
	static int MAP_WIDTH = 30;
	static int MAP_HEIGHT = 20;
	static int MAX_VELOCITY = 1;
	int gridHeight, gridWidth;
	int cannonsToPlace;
	int wallsToPlace;
	int numCannons;
	
	ArrayList<Shot> shot_list;	//  For cannonballs.
	ArrayList<Cannon> cannon_list;
	GameMap map = new GameMap(MAP_WIDTH, MAP_HEIGHT);
	Bitmap wall, castle, cannonball, cannon, grass, water, floor;
	Server server;
	GameState state;
	Mode mode;
	Timer stateTimer, frameTimer;
	GestureDetector gd;
	
	Shape toPlace;
	
	public Game(Context context) {
		super(context);
		getHolder().addCallback(this);
        _thread = new GameLoopThread(getHolder(), this);
        setFocusable(true);
        
		shot_list = new ArrayList<Shot>();
		cannon_list = new ArrayList<Cannon>();
		Player player = new Player();
		server = Server.getInstance();
		server.newGame();
		mode = Mode.CANNONS;
		cannonsToPlace = 3;
		wallsToPlace = 3;
		toPlace = new TShape();
		numCannons = 0;
		stateTimer = new Timer();
		frameTimer = new Timer();
		stateTimer.start();
		frameTimer.start();
		gd = new GestureDetector(this);
		gd.setOnDoubleTapListener(this);
	}
	
	public void init() {
		wall = BitmapFactory.decodeResource(getResources(), R.drawable.wall);
		castle = BitmapFactory.decodeResource(getResources(), R.drawable.castle);
		cannonball = BitmapFactory.decodeResource(getResources(), R.drawable.ball);
		cannon = BitmapFactory.decodeResource(getResources(), R.drawable.cannon);
		grass = BitmapFactory.decodeResource(getResources(), R.drawable.grass);
		water = BitmapFactory.decodeResource(getResources(), R.drawable.water);
		floor = BitmapFactory.decodeResource(getResources(), R.drawable.floor);
		
		gridHeight = getHeight() / MAP_HEIGHT;
		gridWidth = gridHeight;
		
		grass = resizeBitmap(grass, gridHeight, gridWidth);
		wall = resizeBitmap(wall, gridHeight, gridWidth);
		castle = resizeBitmap(castle, 2 * gridHeight, 2 * gridWidth);
		cannon = resizeBitmap(cannon, 2 * gridHeight, 2 * gridWidth);
		cannonball = resizeBitmap(cannonball, (int)(.5 * gridHeight), (int)(.5 * gridWidth));
	}
	
	public void placeWall(Point position, Shape shape) {
		for (Point point : shape.points) {
			map.placeWall(position, shape);
		}
	}
	
	public void deleteFrom(Point position) {
		
	}

	public void updateAnimations() {
		// TODO Auto-generated method stub
		
	}

	public void updateSound() {
		// TODO Auto-generated method stub
		
	}

	public void updateInput() {
		// TODO Auto-generated method stub
		
	}

	public void updateState() {	
		/*
		if(stateTimer.getElapsedTime() > 200) {		// Calls 5 times per second.
			state = server.getGameState();
			if(mode == Mode.REBUILD && state.mode == Mode.CANNONS) {
				cannonsToPlace = 3;
			}
			mode = state.mode;
			stateTimer.start();
		}
		for(int i = 0; i < state.cannons.size(); i++) {
			Point p = state.cannons.get(i);
			Cannon c = new Cannon(p);
			map.insert_at(p, c);
		}
		for(int i = 0; i < state.walls.size(); i++) {
			Point p = state.walls.get(i);
			WallPiece w = new WallPiece(p);
			map.insert_at(p, w);
		}
		for(int i = 0; i < state.shots.size(); i++) {
			Shot s = state.shots.get(i);
			shot_list.add(s);
		} */
		long elapsedTime = frameTimer.getElapsedTime();
		frameTimer.start();
		for(int i = 0; i < shot_list.size(); i++) {
			Shot s = shot_list.get(i);
			Point pos = s.getTarget();
			double distance = Math.sqrt(((pos.get_x() - s.x) * (pos.get_x() - s.x)) + 
					((pos.get_y() - s.y) * (pos.get_y() - s.y)));
			double dMoved = MAX_VELOCITY *(elapsedTime / 1000.0f);
			if(dMoved > distance) {
				map.insert_at(s.target, new BackgroundPiece(GameObject.Type.Grass, s.target));
				/*for(int j = 0; j < cannon_list.size(); i++)  {
					Cannon c = cannon_list.get(j);
					Point p = c.getPosition();
					if(p.get_x() == s.getPosition().get_x()
						&& p.get_y() == s.getPosition().get_y()) {
						c.setFiring(false);
					} 
				} */
				shot_list.remove(i);
			} else {
				shot_list.get(i).x += dMoved * (pos.get_x() - s.x);
				shot_list.get(i).y += dMoved * (pos.get_y() - s.y);
			}
		}
	}
	
	public void updateVideo(Canvas c) {
		for(int i = 0; i < MAP_WIDTH; i++) {
			for(int j = 0; j < MAP_HEIGHT; j++) {
				GameObject toDraw = map.get_at(i, j);
				Point p = toDraw.getPosition();
				switch(toDraw.getType()) {
					case Floor:
						c.drawBitmap(floor, p.get_x() * gridWidth, p.get_y() * gridHeight, null); break;
					case Grass:
						c.drawBitmap(grass, p.get_x() * gridWidth, p.get_y() * gridHeight, null); break;
					case Water:
						c.drawBitmap(water, p.get_x() * gridWidth, p.get_y() * gridHeight, null); break;
					case Cannon:
						c.drawBitmap(cannon, p.get_x() * gridWidth, p.get_y() * gridHeight, null); break;
					case Castle:
						c.drawBitmap(castle, p.get_x() * gridWidth, p.get_y() * gridHeight, null); break;
					case Wall:
						c.drawBitmap(wall, p.get_x() * gridWidth, p.get_y() * gridHeight, null); break;					
				}
			}
		}
		for(int i = 0; i < shot_list.size(); i++) {
			double x = shot_list.get(i).x;
			double y = shot_list.get(i).y;
			c.drawBitmap(cannonball, (float)x * gridWidth, (float)y * gridHeight, null);
		}
		for (Point point : toPlace.points) {
			Point p = new Point(point.get_x() + toPlace.getPosition().get_x(), point.get_y() + toPlace.getPosition().get_y());
			c.drawBitmap(wall, p.get_x() * gridWidth, p.get_y() * gridHeight, null);
		}
	}

	@Override
	public void surfaceChanged(SurfaceHolder holder, int format, int width,
			int height) {
		// TODO Auto-generated method stub
	}

	@Override
	public void surfaceCreated(SurfaceHolder holder) {
		_thread.setRunning(true);
		init();
        _thread.start();	
	}

	@Override
	public void surfaceDestroyed(SurfaceHolder holder) {
		boolean retry = true;
        _thread.setRunning(false);
        while (retry) {
            try {
                _thread.join();
                retry = false;
            } catch (InterruptedException e) {
            }
        }		
	}
	
	private Bitmap resizeBitmap(Bitmap bm, int newHeight, int newWidth) {
		int width = bm.getWidth();
		int height = bm.getHeight();
		float widthScale = ((float)newWidth) / width;
		float heightScale = ((float)newHeight) / height;
		Matrix matrix = new Matrix();
		matrix.postScale(widthScale, heightScale);
		Bitmap resizedbm = Bitmap.createBitmap(bm, 0, 0, width, height, matrix, false);
		return resizedbm;
	}
	
	@Override
	public boolean onTouchEvent(MotionEvent e) {
		/*
		int x = (int)(e.getX() / gridWidth);
		int y = (int)(e.getY() / gridHeight);
		if(mode == Mode.CANNONS){  
			if(cannonsToPlace > 0) {
				if(x > 0 && x < MAP_WIDTH) {
					if(y > 0 && y < MAP_HEIGHT) { 
						Point pos = new Point(x, y);
						map.insert_at(x, y, new Cannon(pos));
						cannonsToPlace--;
					}
				}
			} else {
				mode = Mode.REBUILD;
			}
		}	
		*/
		return gd.onTouchEvent(e);
	}

	@Override
	public boolean onDown(MotionEvent e) {
		return true;
	}

	@Override
	public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX,
			float velocityY) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void onLongPress(MotionEvent e) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX,
			float distanceY) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void onShowPress(MotionEvent e) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public boolean onSingleTapUp(MotionEvent e) {
		Log.d("Rampart", "onSingleTapUp.");
		/*
		if(mode == Mode.BATTLE) {
			int x = (int)(e.getX() / gridWidth);
			int y = (int)(e.getY() / gridHeight);
			if(x > 0 && x < MAP_WIDTH) {
				if(y > 0 && y < MAP_HEIGHT) { 
					for(int i = 0; i < cannon_list.size(); i++) {
						if(!cannon_list.get(i).isFiring()) {
							Log.d("Rampart", "Adding a shot.");
							cannon_list.get(i).setFiring(true);
							shot_list.add(new Shot(new Point(cannon_list.get(i).getPosition()), new Point(x, y)));
							break;
						}
					}
				}
			}
		}  */
		return false;
		
	}

	@Override
	public boolean onDoubleTap(MotionEvent e) {
		Log.d("Rampart", "onDoubleTap.");
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean onDoubleTapEvent(MotionEvent e) {
		Log.d("Rampart", "onDoubleTapEvent.");
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean onSingleTapConfirmed(MotionEvent e) {
			Log.d("Rampart", "onSingleTapConfirmed.");
			int x = (int)(e.getX() / gridWidth);
			int y = (int)(e.getY() / gridHeight);
			if(mode == Mode.CANNONS){  
				if(cannonsToPlace > 0) {
					if(x > 0 && x < MAP_WIDTH) {
						if(y > 0 && y < MAP_HEIGHT) { 
							GameObject go[] = new GameObject[4];
							go[0] = map.get_at(x, y);
							go[1] = map.get_at(x + 1, y);
							go[2] = map.get_at(x, y + 1);
							go[3] = map.get_at(x + 1, y + 1);
							if((go[0].getType() == Type.Grass || go[0].getType() == Type.Floor)
									&& (go[1].getType() == Type.Grass || go[1].getType() == Type.Floor)
									&& (go[2].getType() == Type.Grass || go[2].getType() == Type.Floor)
									&& (go[3].getType() == Type.Grass || go[3].getType() == Type.Floor)) {
								Point pos = new Point(x, y);
								map.insert_at(x, y, new Cannon(pos));
								GameObject g = map.get_at(pos);
								map.insert_at(x + 1, y, g);
								map.insert_at(x, y + 1, g);
								map.insert_at(x + 1, y + 1, g);
								cannon_list.add(new Cannon(pos));
								cannonsToPlace--;
								numCannons++;
								if(cannonsToPlace == 0) {
									mode = Mode.REBUILD;
									return false;
								}
							}
						}
					}
				}
			}
			if (mode == Mode.REBUILD) {
				//Shape L = new LShape();
				//Shape T = new TShape();
				//map.placeWall(new Point(x,y), T);
				if (wallsToPlace > 0) {
					//if (x == toPlace.getPosition().get_x() && y == toPlace.getPosition().get_y()) {
						map.placeWall(new Point(x,y), toPlace);
						toPlace.setPosition(0,0);
						toPlace.rotate();
						--wallsToPlace;
					//} else {
					//	toPlace.setPosition(x, y);
					//}
				} else {
					mode = Mode.BATTLE;
				}

			}
			if(mode == Mode.BATTLE) {
				if(x > 0 && x < MAP_WIDTH) {
					if(y > 0 && y < MAP_HEIGHT) { 
						for(int i = 0; i < cannon_list.size(); i++) {
							if(!cannon_list.get(i).isFiring()) {
								Log.d("Rampart", "Adding a shot.");
								cannon_list.get(i).setFiring(true);
								Point target = new Point(x, y);
								shot_list.add(new Shot(new Point(cannon_list.get(i).getPosition()), target));
								if (map.get_at(target).getType() == Type.Wall) {
									map.delete_at(target);
								}
								break;
							}
						}
					}
				}
			} 
		return false;
	}
}
