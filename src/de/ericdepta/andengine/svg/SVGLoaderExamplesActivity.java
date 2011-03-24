package de.ericdepta.andengine.svg;
/**
 * @author Eric Depta
 * @since 01:14:00 - 28.02.2011
 */
import javax.microedition.khronos.opengles.GL10;

import org.anddev.andengine.engine.Engine;
import org.anddev.andengine.engine.camera.Camera;
import org.anddev.andengine.engine.camera.hud.controls.AnalogOnScreenControl;
import org.anddev.andengine.engine.camera.hud.controls.BaseOnScreenControl;
import org.anddev.andengine.engine.camera.hud.controls.AnalogOnScreenControl.IAnalogOnScreenControlListener;
import org.anddev.andengine.engine.options.EngineOptions;
import org.anddev.andengine.engine.options.EngineOptions.ScreenOrientation;
import org.anddev.andengine.engine.options.resolutionpolicy.RatioResolutionPolicy;
import org.anddev.andengine.entity.scene.Scene;
import org.anddev.andengine.entity.scene.background.ColorBackground;
import org.anddev.andengine.entity.util.FPSLogger;
import org.anddev.andengine.extension.input.touch.controller.MultiTouch;
import org.anddev.andengine.extension.input.touch.controller.MultiTouchController;
import org.anddev.andengine.extension.input.touch.controller.MultiTouchException;
import org.anddev.andengine.extension.physics.box2d.PhysicsWorld;
import org.anddev.andengine.extension.svg.SVGDoc;
import org.anddev.andengine.extension.svg.SVGLoader;
import org.anddev.andengine.extension.svg.util.exception.SVGLoadException;
import org.anddev.andengine.opengl.texture.Texture;
import org.anddev.andengine.opengl.texture.TextureOptions;
import org.anddev.andengine.opengl.texture.region.TextureRegion;
import org.anddev.andengine.opengl.texture.region.TextureRegionFactory;
import org.anddev.andengine.ui.activity.BaseGameActivity;
import org.anddev.andengine.util.Debug;

import android.hardware.SensorManager;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;

public class SVGLoaderExamplesActivity extends BaseGameActivity {
	private static final int CAMERA_WIDTH = 800;
	private static final int CAMERA_HEIGHT = 480;
	private Camera mCamera;
	private boolean mPlaceOnScreenControlsAtDifferentVerticalLocations;
	private PhysicsWorld mPhysicsWorld;
	private Texture mOnScreenControlTexture;
	private TextureRegion mOnScreenControlBaseTextureRegion;
	private TextureRegion mOnScreenControlKnobTextureRegion;
	
	private Body mBallBody;
	private Scene mSVGScene;
	private SVGDoc mSVGDoc;
	private Vector2 mTmpVec = new Vector2(0,0);
	
	private int checked_menu_item = R.id.svg1;
	private SVGExamples mExamples = new SVGExamples(); 
	
	@Override
	public Engine onLoadEngine() {
		this.mCamera = new Camera(0, 0, CAMERA_WIDTH, CAMERA_HEIGHT);
		
		final Engine engine = new Engine(
			new EngineOptions(
				true, 
				ScreenOrientation.LANDSCAPE, 
				new RatioResolutionPolicy(CAMERA_WIDTH, CAMERA_HEIGHT), 
				this.mCamera
			)
		);
		
		//-- Controls
		try {
			if(MultiTouch.isSupported(this)) {
				engine.setTouchController(new MultiTouchController());
				if(MultiTouch.isSupportedDistinct(this)) {
					Toast.makeText(this, "MultiTouch detected --> Both controls will work properly!", Toast.LENGTH_LONG).show();
				} else {
					this.mPlaceOnScreenControlsAtDifferentVerticalLocations = true;
					Toast.makeText(this, "MultiTouch detected, but your device has problems distinguishing between fingers.\n\nControls are placed at different vertical locations.", Toast.LENGTH_LONG).show();
				}
			} else {
				Toast.makeText(this, "Sorry your device does NOT support MultiTouch!\n\n(Falling back to SingleTouch.)\n\nControls are placed at different vertical locations.", Toast.LENGTH_LONG).show();
			}
		} catch (final MultiTouchException e) {
			Toast.makeText(this, "Sorry your Android Version does NOT support MultiTouch!\n\n(Falling back to SingleTouch.)\n\nControls are placed at different vertical locations.", Toast.LENGTH_LONG).show();
		}
		
		//-- Physic
		this.mPhysicsWorld = new PhysicsWorld(new Vector2(0, SensorManager.GRAVITY_EARTH), false);
		
		return engine;
	}
	
	@Override
	public void onLoadResources() {
		TextureRegionFactory.setAssetBasePath("gfx/");
		//-- Controls
		this.mOnScreenControlTexture = new Texture(256, 128, TextureOptions.BILINEAR_PREMULTIPLYALPHA);
		this.mOnScreenControlBaseTextureRegion = TextureRegionFactory.createFromAsset(this.mOnScreenControlTexture, this, "onscreen_control_base.png", 0, 0);
		this.mOnScreenControlKnobTextureRegion = TextureRegionFactory.createFromAsset(this.mOnScreenControlTexture, this, "onscreen_control_knob.png", 128, 0);
		
		this.mEngine.getTextureManager().loadTextures(this.mOnScreenControlTexture);
	}
	
	@Override
	public Scene onLoadScene() {
		this.mEngine.registerUpdateHandler(new FPSLogger());
		
		this.mSVGScene = new Scene(1);
		this.mSVGScene.setBackground(new ColorBackground(1,1,1));
		
		//-- SVG
		this.mSVGDoc = new SVGDoc(this, this.mPhysicsWorld, this.mEngine.getTextureManager());
		this.loadSVG();
		this.mSVGScene.attachChild(this.mSVGDoc);
		
		//-- Controls
		//--- speed
		final int x1 = 0;
		final int y1 = CAMERA_HEIGHT - this.mOnScreenControlBaseTextureRegion.getHeight();
		final AnalogOnScreenControl velocityOnScreenControl = new AnalogOnScreenControl(x1, y1, this.mCamera, this.mOnScreenControlBaseTextureRegion, this.mOnScreenControlKnobTextureRegion, 0.1f, new IAnalogOnScreenControlListener() {
			@Override
			public void onControlChange(final BaseOnScreenControl pBaseOnScreenControl, float pValueX, float pValueY) {
				if(mBallBody != null){
					if(pValueX!=0 || pValueY!=0){
						mBallBody.setLinearVelocity(mTmpVec.set(pValueX * 15, pValueY * 15));
					}
				}
			}
			
			@Override
			public void onControlClick(final AnalogOnScreenControl pAnalogOnScreenControl) {}
		});
		velocityOnScreenControl.getControlBase().setBlendFunction(GL10.GL_SRC_ALPHA, GL10.GL_ONE_MINUS_SRC_ALPHA);
		velocityOnScreenControl.getControlBase().setAlpha(0.5f);
		this.mSVGScene.setChildScene(velocityOnScreenControl);

		//--- impulse
		final int y2 = (this.mPlaceOnScreenControlsAtDifferentVerticalLocations) ? 0 : y1;
		final int x2 = CAMERA_WIDTH - this.mOnScreenControlBaseTextureRegion.getWidth();
		final AnalogOnScreenControl rotationOnScreenControl = new AnalogOnScreenControl(x2, y2, this.mCamera, this.mOnScreenControlBaseTextureRegion, this.mOnScreenControlKnobTextureRegion, 0.1f, new IAnalogOnScreenControlListener() {
			@Override
			public void onControlChange(final BaseOnScreenControl pBaseOnScreenControl, final float pValueX, final float pValueY) {
				if(mBallBody != null){
					if(pValueX!=0 || pValueY!=0){
						mBallBody.applyLinearImpulse(mTmpVec.set(pValueX * 10, pValueY * 10).cpy(), mTmpVec.set(0, 0));
					}
				}
			}

			@Override
			public void onControlClick(final AnalogOnScreenControl pAnalogOnScreenControl) {}
		});
		rotationOnScreenControl.getControlBase().setBlendFunction(GL10.GL_SRC_ALPHA, GL10.GL_ONE_MINUS_SRC_ALPHA);
		rotationOnScreenControl.getControlBase().setAlpha(0.5f);
		velocityOnScreenControl.setChildScene(rotationOnScreenControl);
		
		this.mSVGScene.registerUpdateHandler(this.mPhysicsWorld);
		
		return this.mSVGScene;
	}
	
	private void loadSVG(){
		try {
			this.removeDoc();
			final SVGLoader svgLoader = new SVGLoader(this.mSVGDoc);
			svgLoader.loadFromAsset(this.mExamples.get(this.checked_menu_item));
			this.onLoadSVG();
		} catch (final SVGLoadException svgle) {
			Debug.e(svgle);
		}
	}
	
	private void removeDoc(){
		this.mSVGDoc.removeElements();
	}
	
    private void onLoadSVG(){
		this.mBallBody = this.mSVGDoc.getElement("Ball").getBody();
	}
    
	@Override
	public void onLoadComplete() {}
	
	
	/*
	 * Menu
	 */
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);
		getMenuInflater().inflate(R.menu.svgmenu, menu);
		menu.findItem(this.checked_menu_item).setChecked(true);
		return true;
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		if (item.isChecked()){
			item.setChecked(false);
		}else{
			item.setChecked(true);
			this.checked_menu_item = item.getItemId();
			this.loadSVG();
		}
		return true;
	}

	
}