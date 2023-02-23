package com.vovangames.editor;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.ScreenUtils;

public class Editor extends ApplicationAdapter implements InputProcessor {
	Stage s;
	Stage ui;
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
		c = (OrthographicCamera) s.getCamera();
		x = new Texture("arrowX.png");
		y = new Texture("arrowY.png");
		wall = new Texture("wall.png");
		arrowX = new Image(x);
		arrowX.setSize(500, 10);
		arrowY = new Image(y);
		arrowY.setSize(10, 500);
		s.getDebugColor().set(Color.CYAN);

		Gdx.input.setInputProcessor(this);
	}

	private void addWall(float x, float y) {
		Image w = new Image(wall);
		w.setPosition(x, y);
		s.addActor(w);
	}

	private void select() {
		Actor a = s.hit(Gdx.input.getX(), Gdx.graphics.getHeight() - Gdx.input.getY(), true);

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

	@Override
	public void render () {
		ScreenUtils.clear(0, 0, 0, 1);
		s.draw();
		if (Gdx.input.isButtonJustPressed(Input.Buttons.RIGHT)) {
			addWall(Gdx.input.getX(),Gdx.graphics.getHeight() - Gdx.input.getY());
		}
		if (Gdx.input.isButtonJustPressed(Input.Buttons.LEFT)) {
			select();
		}
	}

	@Override
	public void resize(int width, int height) {
		c.viewportWidth = width;
		c.viewportHeight = height;
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
