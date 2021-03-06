package eu.lestard.fxzeug.usability;

import javafx.beans.property.IntegerProperty;
import javafx.beans.property.ReadOnlyIntegerWrapper;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;

import java.io.File;
import java.io.FileOutputStream;
import java.net.URL;

/**
 * @author manuel.mauky
 */
class ScalingHelper {
	private IntegerProperty minFontSize = new SimpleIntegerProperty(6);
	private IntegerProperty maxFontSize = new SimpleIntegerProperty(40);
	IntegerProperty defaultFontSize = new SimpleIntegerProperty(12);
	
	ReadOnlyIntegerWrapper fontSize = new ReadOnlyIntegerWrapper(defaultFontSize.get());

	private boolean mouseWheelScalingActive = false;
	private boolean keyboardScalingActive = false;
	
	private MouseWheelScalingHelper mouseWheelScalingHelper = new MouseWheelScalingHelper(this::scaleUp,
			this::scaleDown);
	
	private KeyboardScalingHelper keyboardScalingHelper = new KeyboardScalingHelper(this::scaleUp, this::scaleDown, this::scaleToDefault);
	
	private final URL tempStylesheetPath;
	
	ScalingHelper() {
		tempStylesheetPath = createTempStyleSheetFile();
		
		minFontSize.addListener((observable, oldValue, newValue) -> {
			if (newValue.intValue() > maxFontSize.get()) {
				minFontSize.set(oldValue.intValue());
			}
			
			if (newValue.intValue() > fontSize.get()) {
				fontSize.set(newValue.intValue());
			}
		});
		
		maxFontSize.addListener((observable, oldValue, newValue) -> {
			if (newValue.intValue() < minFontSize.get()) {
				maxFontSize.set(oldValue.intValue());
			}
			
			if (newValue.intValue() < fontSize.get()) {
				fontSize.set(newValue.intValue());
			}
		});
		
		fontSize.addListener((observable, oldValue, newValue) -> {
			if (newValue.intValue() < minFontSize.get()) {
				fontSize.set(minFontSize.get());
			}
			
			if (newValue.intValue() > maxFontSize.get()) {
				fontSize.set(maxFontSize.get());
			}
		});
		
		defaultFontSize.addListener((observable, oldValue, newValue) -> {
			if (newValue.intValue() < minFontSize.get()) {
				defaultFontSize.set(oldValue.intValue());
			}

			if (newValue.intValue() > maxFontSize.get()) {
				defaultFontSize.set(newValue.intValue());
			}
		});
	}
	
	
	void initScene(final Scene scene) {
		initStyleSheet(scene);
		
		if (mouseWheelScalingActive) {
			mouseWheelScalingHelper.initScene(scene);
		}
		
		if(keyboardScalingActive) {
			keyboardScalingHelper.initScene(scene);
		}
	}
	
	String generateStyleSheet() {
		StringBuilder stylesheet = new StringBuilder();
        
        for(int i=1 ; i<100 ; i++) {
            stylesheet.append(".");
            stylesheet.append(getStyleClass(i));
            stylesheet.append(" {\n\t-fx-font-size: ");
			stylesheet.append(i);
			stylesheet.append("px;\n}\n");
        }
        
		return stylesheet.toString();
	}
	
	private URL createTempStyleSheetFile() {
		try {
			final File cssfile = File.createTempFile("fx-zeug-scaling", ".css");
			cssfile.deleteOnExit();
			
			try (FileOutputStream out = new FileOutputStream(cssfile)) {
				out.write(generateStyleSheet().getBytes());
				out.close();
			}
			
			return cssfile.toURI().toURL();
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
	
	private void initStyleSheet(final Scene scene) {
		scene.getStylesheets().add(tempStylesheetPath.toExternalForm());
		
		if (scene.getRoot() != null) {
			scene.getRoot().getStyleClass().add(getCurrentStyleClass());
		}
		
		fontSize.addListener((observable, oldValue, newValue) -> {
			final Parent root = scene.getRoot();
			if (root != null) {
				root.getStyleClass().removeAll(getStyleClass(oldValue.intValue()));
				root.getStyleClass().add(getCurrentStyleClass());
			}
		});
		
		scene.rootProperty().addListener((observable, oldValue, newValue) -> {
			if (oldValue != null) {
				oldValue.getStyleClass().removeAll(getCurrentStyleClass());
			}
			
			if (newValue != null) {
				if (!newValue.getStyleClass().contains(getCurrentStyleClass())) {
					newValue.getStyleClass().add(getCurrentStyleClass());
				}
			}
		});
	}
	
	String getStyleClass(int fontSize) {
		return "fx_zeug_scaling_" + fontSize;
	}
	
	String getCurrentStyleClass() {
		return getStyleClass(fontSize.get());
	}
	
	
	
	void scaleUp() {
		final int currentFontSize = fontSize.get();
		
		if (currentFontSize + 1 <= maxFontSize.get()) {
			fontSize.set(currentFontSize + 1);
		}
	}
	
	void scaleDown() {
		final int currentFontSize = fontSize.get();
		
		if (currentFontSize - 1 >= minFontSize.get()) {
			fontSize.set(currentFontSize - 1);
		}
	}
	
	void scaleToDefault() {
		fontSize.set(defaultFontSize.get());
	}
	
	void enableMouseWheel(KeyCode... modifier) {
		mouseWheelScalingActive = true;
		mouseWheelScalingHelper.enable(modifier);
	}
	
	void disableMouseWheel() {
		mouseWheelScalingActive = false;
	}
	
	
	void enableKeyboardScaling(KeyCodeCombination scaleUp, KeyCodeCombination scaleDown, KeyCodeCombination scaleToDefault) {
		keyboardScalingActive = true;
		keyboardScalingHelper.enable(scaleUp, scaleDown, scaleToDefault);
	}
	
	void disableKeyboardScaling() {
		keyboardScalingActive = false;
	}
}
