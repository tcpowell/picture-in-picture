package com.pip;

import com.google.inject.Provides;
import javax.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.GameState;
import net.runelite.api.events.ClientTick;
import net.runelite.api.events.GameStateChanged;
import net.runelite.api.events.GameTick;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.ui.ClientUI;
import net.runelite.client.ui.DrawManager;
import java.awt.*;
import java.awt.image.*;
import java.awt.event.*;
import java.util.concurrent.ScheduledExecutorService;
import java.util.function.Consumer;
import javax.swing.*;


@Slf4j
@PluginDescriptor(
		name = "Picture In Picture",
		description = "Displays picture in picture mode when RuneLite is not in focus",
		tags = {"pip", "picture", "display", "afk"}
)
public class PictureInPicturePlugin extends Plugin
{
	private static boolean focused = true;
	private static boolean pipUp = false;
	private JFrame pipFrame = null;
	private JLabel lbl = null;
	private int clientTick = 0;
	private int pipWidth, pipHeight;
	private double pipScale;

	@Inject
	private ClientUI clientUi;

	@Inject
	private ScheduledExecutorService executor;

	@Inject
	private DrawManager drawManager;

	@Inject
	private PictureInPictureConfig config;

	@Override
	protected void startUp() throws Exception
	{
		log.debug("PIP started!");
	}

	@Override
	protected void shutDown() throws Exception
	{
		log.debug("PIP stopped!");
		destroyPip();
	}

	@Subscribe
	public void onGameStateChanged(GameStateChanged gameStateChanged)
	{
		if (gameStateChanged.getGameState() == GameState.LOGIN_SCREEN)
			destroyPip();
	}

	@Subscribe
	public void onGameTick(GameTick tick)
	{
		if (!focused) {
			Window window = javax.swing.FocusManager.getCurrentManager().getActiveWindow();
			if (window == null) {
				if (pipFrame == null) {
					initializePip();
					log.debug("PIP initialized");
				}
			}
		}
	}

	@Subscribe
	public void onClientTick(ClientTick event)
	{
		if(clientTick % config.redrawRate().toInt()==0) {
			clientTick = 0;

			if (focused != clientUi.isFocused()) {
				focused = clientUi.isFocused();
				if (focused)
					destroyPip();
			}
			if (!focused) {
				if (pipFrame != null) {
					if (pipUp) {
						updatePip();
						//log.debug("PIP updated");
					}
				}
			}
		}
		clientTick++;
	}

	@Provides
	PictureInPictureConfig provideConfig(ConfigManager configManager)
	{
		return configManager.getConfig(PictureInPictureConfig.class);
	}

	private void startPip(Image image) {
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				if (config.limitedDimension().toString().equals("Width")) {
					pipWidth = config.targetSize().getWidth();
					pipScale = (double) pipWidth / (double) image.getWidth(null) ;
					pipHeight = (int) (image.getHeight(null) * pipScale);
				}
				else {
					pipHeight = config.targetSize().getHeight();
					pipScale = (double) pipHeight / (double) image.getHeight(null);
					pipWidth = (int) (image.getWidth(null) * pipScale);
				}

				Image img = pipScale(image);
				ImageIcon icon=new ImageIcon(img);

				pipFrame=new JFrame();
				pipFrame.setFocusableWindowState(false);
				pipFrame.setType(Window.Type.UTILITY);
				pipFrame.setLayout(new FlowLayout());
				pipFrame.setSize(img.getWidth(null),img.getHeight(null));

				lbl=new JLabel();
				lbl.setIcon(icon);

				pipFrame.setUndecorated(true);
				pipFrame.add(lbl);
				pipFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
				pipFrame.setAlwaysOnTop(true);

				pipFrame.addMouseListener(new MouseAdapter() {
					public void mousePressed(MouseEvent e) {
						pipClicked();
					}
				});

				//get screen info
				GraphicsConfiguration gc = clientUi.getGraphicsConfiguration();
				Rectangle bounds = gc.getBounds();
				Insets screenInsets = Toolkit.getDefaultToolkit().getScreenInsets(gc);
				Rectangle effectiveScreenArea = new Rectangle();
				effectiveScreenArea.x = bounds.x + screenInsets.left;
				effectiveScreenArea.y = bounds.y + screenInsets.top;
				effectiveScreenArea.height = bounds.height - screenInsets.top - screenInsets.bottom;
				effectiveScreenArea.width = bounds.width - screenInsets.left - screenInsets.right;

				//set location
				if (config.quadrantID().toInt() == 1)
					pipFrame.setLocation(effectiveScreenArea.width - pipWidth - config.paddingX(),config.paddingY());
				else if (config.quadrantID().toInt() == 2)
					pipFrame.setLocation(config.paddingX(),config.paddingY());
				else if (config.quadrantID().toInt() == 3)
					pipFrame.setLocation(config.paddingX(),effectiveScreenArea.height - pipHeight - config.paddingY());
				else
					pipFrame.setLocation(effectiveScreenArea.width - pipWidth - config.paddingX(),effectiveScreenArea.height - pipHeight - config.paddingY());

				// Display the window.
				pipFrame.pack();
				pipFrame.setVisible(true);
				pipUp = true;
			}
		});
	}

	private void destroyPip() {
		if (pipFrame != null) {
			pipFrame.setVisible(false);
			pipFrame.dispose();
			pipFrame = null;
			pipUp = false;
		}
	}

	private void pipClicked() {
		log.debug("PIP Clicked");
		if (config.clickAction().clickMode() == 0) {
			destroyPip();
			clientUi.requestFocus();
		}
		else if (config.clickAction().clickMode() == 1) {
			destroyPip();
			clientUi.forceFocus();
		}
	}

	//runs first to initialize pip
	private void initializePip() {
		Consumer<Image> imageCallback = (img) ->
		{
			executor.submit(() -> startPip(img));
		};
		drawManager.requestNextFrameListener(imageCallback);
	}

	//updates if pip is already up
	private void updatePip() {
		Consumer<Image> imageCallback = (img) ->
		{
			executor.submit(() -> updatePip(img));
		};
		drawManager.requestNextFrameListener(imageCallback);
	}

	//update image
	private void updatePip (Image image) {
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				Image img = pipScale(image);
				ImageIcon icon = new ImageIcon(img);
				icon.getImage().flush();
				lbl.setIcon(icon);
			}
		});
	}

	private Image pipScale(Image originalImage) {

		int samples = config.renderQuality().getRedraw();
		RenderingHints hints = new RenderingHints(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		hints.add(new RenderingHints(RenderingHints.KEY_RENDERING, config.renderQuality().getQuality()));
		hints.add(new RenderingHints(RenderingHints.KEY_INTERPOLATION, config.renderQuality().getHint()));

		if (pipScale>1)
			return originalImage;

		BufferedImage returnImage = (BufferedImage) originalImage;

		int w = originalImage.getWidth(null);
		int h = originalImage.getHeight(null);
		int incW = (w - pipWidth) / samples;
		int incH = (h - pipHeight) / samples;

		for (int i=1; i<=samples; i++) {

			if (i==samples) {
				w = pipWidth;
				h = pipHeight;
			}
			else {
				w -= incW;
				h -= incH;
			}

			BufferedImage tempImage = new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB);
			Graphics2D g2 = tempImage.createGraphics();
			g2.setRenderingHints(hints);
			g2.drawImage(returnImage, 0, 0, w, h, null);
			g2.dispose();
			returnImage = tempImage;
		}
		return returnImage;
	}
}
