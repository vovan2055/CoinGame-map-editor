package com.vovangames.editor;

import com.badlogic.gdx.*;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.StringBuilder;
import javax.swing.*;
import javax.swing.filechooser.FileFilter;
import javax.swing.filechooser.FileNameExtensionFilter;


public class Editor extends ApplicationAdapter implements InputProcessor {
	Stage s;
	Stage ui;
	Label debug;
	Actor selected;
	Image arrowX, arrowY;
	OrthographicCamera c;
	Texture x, y, wall;
	float offset = 30;
	boolean scrollX = false;
	boolean scrollY = false;
	boolean scale = false;
	
	@Override
	public void create () {
		s = new Stage();
		ui = new Stage();
		c = (OrthographicCamera) s.getCamera();
		x = new Texture("arrowX.png");
		y = new Texture("arrowY.png");
		wall = new Texture("wall.png");
		arrowX = new Image(x);
		arrowX.setSize(500, 10);
		arrowY = new Image(y);
		arrowY.setSize(10, 500);
		s.getDebugColor().set(Color.CYAN);
		debug = new Label("", new Label.LabelStyle(new BitmapFont(), Color.WHITE));
		ui.addActor(debug);

		InputMultiplexer m = new InputMultiplexer();
		m.addProcessor(s);
		m.addProcessor(ui);
		m.addProcessor(this);
		Gdx.input.setInputProcessor(m);
	}

	private void addWall(float x, float y) {
		Image w = new Image(wall);
		Vector2 v = s.screenToStageCoordinates(new Vector2(x, Gdx.graphics.getHeight() - y));
		w.setPosition(v.x, v.y);
		s.addActor(w);
	}

	private void select() {
		System.out.println("select");
		Vector2 v = s.screenToStageCoordinates(new Vector2(Gdx.input.getX(), Gdx.input.getY()));
		Actor a = s.hit(v.x, v.y, true);

		if (a != null && !(a == arrowX || a == arrowY) && !(scrollX || scrollY)) {
			if (selected != null) selected.setDebug(false);
			unselect();
			selected = a;
			s.addActor(arrowX);
			s.addActor(arrowY);
			arrowX.setPosition(a.getX() + a.getWidth() + offset * c.zoom, a.getY());
			arrowY.setPosition(a.getX(), a.getY() + a.getHeight()  + offset * c.zoom);
			selected.setDebug(true);
		}
	}

	private void unselect() {
		if (selected != null) selected.setDebug(false);
		selected = null;
		arrowX.remove();
		arrowY.remove();
	}

	private void readControls() {
		if (Gdx.input.isKeyPressed(Input.Keys.W)) c.translate(0, c.zoom * 5);
		if (Gdx.input.isKeyPressed(Input.Keys.A)) c.translate(-c.zoom * 5, 0);
		if (Gdx.input.isKeyPressed(Input.Keys.S)) c.translate(0, -c.zoom * 5);
		if (Gdx.input.isKeyPressed(Input.Keys.D)) c.translate(c.zoom * 5, 0);
		if (Gdx.input.isKeyJustPressed(Input.Keys.Q)) {
			StringBuilder b = new StringBuilder();
			for (Actor a : s.getActors()) {
				b.append(a.getX() + ";");
				b.append(a.getY() + ";");
				b.append(a.getWidth() + ";");
				b.append(a.getHeight());
				if (s.getActors().indexOf(a, true) < s.getActors().size - 1) b.append("\n");
			}
			String output = b.toString();
			System.out.println(output);
			FileHandle h = Gdx.files.external("outMap.cmap");
			h.writeString(output, false);
			System.out.println(h.path());
		}
		if (Gdx.input.isKeyPressed(Input.Keys.NUM_0)) c.position.set(0, 0, 0);
		if (Gdx.input.isKeyJustPressed(Input.Keys.L)) new Thread() {
			@Override
			public void run() {
				JFileChooser chooser = new JFileChooser(Gdx.files.getExternalStoragePath());
				FileFilter f = new FileNameExtensionFilter(".cmap file", ".cmap");
				chooser.setFileFilter(f);
				chooser.addChoosableFileFilter(f);
				int code = chooser.showOpenDialog(null);
				if (code == JFileChooser.APPROVE_OPTION)  {
					String[] objects = Gdx.files.absolute(chooser.getSelectedFile().getAbsolutePath()).readString().split("\n");
					System.out.println(objects[0]);
				}

			}
		}.run();

	}

	@Override
	public void render () {
		ScreenUtils.clear(0, 0, 0, 1);
		debug.setText("Right click - new wall\nLeft click - select & drag\nSpacebar - shange mode(scale, move)\nQ - Save\nMouse wheel - zoom\n0 - set camera position to (0, 0)" + "FPS: " + Gdx.graphics.getFramesPerSecond());
		if (selected != null) {
			debug.setText(debug.getText() + "\n" + "X: " + selected.getX() + "\n" + "Y: " + selected.getY() + "\n" + "W: " + selected.getWidth() + "\n" + "H: " + selected.getHeight());
		}
		s.draw();
		ui.draw();
		if (Gdx.input.isButtonJustPressed(Input.Buttons.RIGHT) && s.hit(Gdx.input.getX(), Gdx.input.getY(), true) == null) {
			addWall(Gdx.input.getX(),Gdx.graphics.getHeight() - Gdx.input.getY());
		}
		readControls();
	}

	@Override
	public void resize(int width, int height) {
		c.viewportWidth = s.getCamera().viewportWidth = width;
		c.viewportHeight = s.getCamera().viewportHeight = height;
		Array<Actor> actors = s.getActors();
		s = new Stage();
		s.getDebugColor().set(Color.CYAN);
		for (Actor a : actors) {
			s.addActor(a);
		}
		ui.getCamera().viewportWidth = width;
		ui.getCamera().viewportHeight = height;
		debug.setY(Gdx.graphics.getHeight() - 100);
	}

	@Override
	public void dispose () {
	}

	@Override
	public boolean keyDown(int keycode) {
		if (keycode == Input.Keys.SPACE) scale = !scale;
		return false;
	}

	@Override
	public boolean keyUp(int keycode) {
		return false;
	}

	@Override
	public boolean keyTyped(char character) {
		return false;
	}

	@Override
	public boolean touchDown(int screenX, int screenY, int pointer, int button) {
		Vector2 v = s.screenToStageCoordinates(new Vector2(screenX, screenY));
		if (button == Input.Buttons.LEFT && s.hit(v.x, v.y, true) == arrowX) scrollX = true;
		else if (button == Input.Buttons.LEFT && s.hit(v.x, v.y, true) == arrowY) scrollY = true;
		else if (button == Input.Buttons.LEFT && s.hit(v.x, v.y, true) != null) select();
		else unselect();
		return false;
	}

	@Override
	public boolean touchUp(int screenX, int screenY, int pointer, int button) {
		if (scrollX) scrollX = false;
		if (scrollY) scrollY = false;
		return false;
	}

	@Override
	public boolean touchDragged(int screenX, int screenY, int pointer) {
		if (scrollX && selected != null) {
			if (scale) {
				selected.setWidth(selected.getWidth() + Gdx.input.getDeltaX() * c.zoom);
			} else selected.setX(selected.getX() + Gdx.input.getDeltaX() * c.zoom);
			arrowX.setX(selected.getX() + selected.getWidth() + offset * c.zoom);
			arrowY.setX(selected.getX(Align.center), Align.center);
		}
		if (scrollY && selected != null) {
			if (scale) {
				selected.setHeight(selected.getHeight() - Gdx.input.getDeltaY() * c.zoom);
			} else selected.setY(selected.getY() - Gdx.input.getDeltaY() * c.zoom);
			arrowY.setY(selected.getY() + selected.getHeight() + offset * c.zoom);
			arrowX.setY(selected.getY(Align.center), Align.center);
		}
		return false;
	}

	@Override
	public boolean mouseMoved(int screenX, int screenY) {
		return false;
	}

	@Override
	public boolean scrolled(float amountX, float amountY) {
		c.zoom += amountY;
		arrowX.setScale(c.zoom);
		arrowY.setScale(c.zoom);
		return false;
	}
}
