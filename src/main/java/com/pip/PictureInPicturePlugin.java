package com.pip;

import com.google.inject.Provides;
import javax.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.*;
import net.runelite.api.events.ClientTick;
import net.runelite.api.events.GameStateChanged;
import net.runelite.api.events.GameTick;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.ui.ClientUI;
import net.runelite.client.ui.DrawManager;
import net.runelite.client.ui.FontManager;

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
	private pipBar leftBar, rightBar;

	private int clientTick = 0;
	private int pipWidth, pipHeight;
	private double pipScale;
	private int maxHealth, currentHealth, maxPrayer, currentPrayer;
	private Color[] healthColor;
	private static final int GAP = 3;
	private static final int BAR_WIDTH = 20;

	Skill leftSkill, rightSkill;

	private static final Color PRAYER_COLOR = new Color(32, 160, 160);
	private static final Color PRAYER_BG_COLOR = new Color(10, 50, 50);
	private static final Color[] PRAYER = {PRAYER_COLOR, PRAYER_BG_COLOR};

	private static final Color HEALTH_COLOR = new Color(160, 32, 0);
	private static final Color HEALTH_BG_COLOR = new Color(50, 10, 0);
	private static final Color[] HEALTH = {HEALTH_COLOR, HEALTH_BG_COLOR};

	private static final Color POISONED_COLOR = new Color(0, 160, 0);
	private static final Color POISONED_BG_COLOR = new Color(0, 50, 0);
	private static final Color[] POISONED = {POISONED_COLOR, POISONED_BG_COLOR};

	private static final Color VENOMED_COLOR = new Color(0, 90, 0);
	private static final Color VENOMED_BG_COLOR = new Color(0, 25, 0);
	private static final Color[] VENOMED = {VENOMED_COLOR, VENOMED_BG_COLOR};

	private static final Color DISEASE_COLOR = new Color(200, 160, 64);
	private static final Color DISEASE_BG_COLOR = new Color(63, 50, 20);
	private static final Color[] DISEASE = {DISEASE_COLOR, DISEASE_BG_COLOR};

	private class pipBar extends JPanel {

		int maxLevel;
		int currentLevel;
		Color[] colors;
		int barWidth;

		private pipBar(int maxLevel, int currentLevel, Color[] colors, int barWidth) {
			this.maxLevel = maxLevel;
			this.currentLevel = currentLevel;
			this.colors = colors;
			this.barWidth = barWidth;
			setFont(FontManager.getRunescapeSmallFont());
		}

		private void updateBar(int maxLevel, int currentLevel, Color[] colors, int barWidth) {
			this.maxLevel = maxLevel;
			this.currentLevel = currentLevel;
			this.colors = colors;
			this.barWidth = barWidth;
			repaint();
		}

		@Override
		protected void paintComponent(Graphics g) {
			super.paintComponent(g);
			g.setColor(colors[0]);
			g.fillRect(0,0,barWidth,pipHeight);
			int bgHeight = (int) ((maxLevel - currentLevel)*pipHeight/((float) maxLevel));
			g.setColor(colors[1]);
			g.fillRect(0, 0, barWidth, bgHeight);

			if (config.barText()) {
				g.setFont(FontManager.getRunescapeSmallFont());

				String text = String.valueOf(currentLevel);
				int y = 20;
				int x = BAR_WIDTH / 2 - g.getFontMetrics().stringWidth(text) / 2;

				//text outline
				g.setColor(Color.BLACK);
				g.drawString(text, x, y + 1);
				g.drawString(text, x, y - 1);
				g.drawString(text, x + 1, y);
				g.drawString(text, x - 1, y);

				//text
				g.setColor(Color.WHITE);
				g.drawString(text, x, y);
			}
		}

		@Override
		public Dimension getPreferredSize() {
			return new Dimension(20, pipHeight); // appropriate constants
		}
	}

	@Inject
	private ClientUI clientUi;

	@Inject
	private Client client;

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
					updateHitpoints();
					updatePrayer();
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

		//split this from the pip (bars can update more frequently if needed
		if (!focused) {
			if (pipFrame != null) {
				if (pipUp) {
					updateHitpoints();
					updatePrayer();
					updateBars();
				}
			}
		}
	}

	@Provides
	PictureInPictureConfig provideConfig(ConfigManager configManager)
	{
		return configManager.getConfig(PictureInPictureConfig.class);
	}

	private void updateHitpoints(){
		currentHealth = client.getBoostedSkillLevel(Skill.HITPOINTS);
		maxHealth = client.getRealSkillLevel(Skill.HITPOINTS);
		healthColor = HEALTH;
		int poisonState = client.getVar(VarPlayer.IS_POISONED);
		if (poisonState >= 1000000)
			healthColor = VENOMED;
		else if (poisonState > 0)
			healthColor = POISONED;
		else if (client.getVar(VarPlayer.DISEASE_VALUE) > 0)
			healthColor = DISEASE;
	}
	private void updatePrayer(){
		currentPrayer = client.getBoostedSkillLevel(Skill.PRAYER);
		maxPrayer = client.getRealSkillLevel(Skill.PRAYER);
	}

	private void startPip(Image image) {

		int position = config.barPosition().getPosition();
		leftSkill = config.leftBar().getSkill();
		rightSkill = config.rightBar().getSkill();

		final int offset = ((leftSkill != null) ? GAP + BAR_WIDTH : 0) + ((rightSkill != null) ? GAP + BAR_WIDTH : 0);

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
				pipFrame.setLayout(new FlowLayout(FlowLayout.LEFT, GAP, GAP));
				pipFrame.setSize(img.getWidth(null)+offset,img.getHeight(null));
				lbl=new JLabel();
				lbl.setIcon(icon);
				pipFrame.setUndecorated(true);

				//pull in bar info from config
				if (leftSkill == Skill.HITPOINTS)
					leftBar = new pipBar(maxHealth, currentHealth, healthColor, BAR_WIDTH);
				else if (leftSkill == Skill.PRAYER)
					leftBar = new pipBar(maxPrayer, currentPrayer, PRAYER, BAR_WIDTH);
				if (rightSkill == Skill.HITPOINTS)
					rightBar = new pipBar(maxHealth, currentHealth, healthColor, BAR_WIDTH);
				else if (rightSkill == Skill.PRAYER)
					rightBar = new pipBar(maxPrayer, currentPrayer, PRAYER, BAR_WIDTH);

				//set the order of bars and pip window
				if (position == 0) {
					if (leftSkill != null)
						pipFrame.add(leftBar);
					if (rightSkill != null)
						pipFrame.add(rightBar);
					pipFrame.add(lbl);
				}
				else if (position ==1) {
					pipFrame.add(lbl);
					if (leftSkill != null)
						pipFrame.add(leftBar);
					if (rightSkill != null)
						pipFrame.add(rightBar);
				}
				else {
					if (leftSkill != null)
						pipFrame.add(leftBar);
					pipFrame.add(lbl);
					if (rightSkill != null)
						pipFrame.add(rightBar);
				}

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
					pipFrame.setLocation(effectiveScreenArea.width - pipWidth - config.paddingX() - offset,config.paddingY());
				else if (config.quadrantID().toInt() == 2)
					pipFrame.setLocation(config.paddingX(),config.paddingY());
				else if (config.quadrantID().toInt() == 3)
					pipFrame.setLocation(config.paddingX(),effectiveScreenArea.height - pipHeight - config.paddingY());
				else
					pipFrame.setLocation(effectiveScreenArea.width - pipWidth - config.paddingX() - offset,effectiveScreenArea.height - pipHeight - config.paddingY());

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

	//update bars
	private void updateBars () {
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				if (leftSkill == Skill.HITPOINTS)
					leftBar.updateBar(maxHealth, currentHealth, healthColor, BAR_WIDTH);
				else if (leftSkill == Skill.PRAYER)
					leftBar.updateBar(maxPrayer, currentPrayer, PRAYER, BAR_WIDTH);
				if (rightSkill == Skill.HITPOINTS)
					rightBar.updateBar(maxHealth, currentHealth, healthColor, BAR_WIDTH);
				else if (rightSkill == Skill.PRAYER)
					rightBar.updateBar(maxPrayer, currentPrayer, PRAYER, BAR_WIDTH);
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
