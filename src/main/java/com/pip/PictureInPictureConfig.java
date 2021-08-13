package com.pip;

import net.runelite.api.Skill;
import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;
import net.runelite.client.config.ConfigSection;

import java.awt.RenderingHints;

@ConfigGroup("pip")
public interface PictureInPictureConfig extends Config
{
	enum quadrant
	{
		BOTTOM_RIGHT("Bottom Right", 4),
		BOTTOM_LEFT("Bottom Left", 3),
		TOP_LEFT("Top Left", 2),
		TOP_RIGHT("Top Right", 1)
		;

		private String value;
		private int id;

		quadrant(String value, int id)
		{
			this.value = value;
			this.id = id;
		}

		@Override
		public String toString()
		{
			return this.value;
		}

		public int toInt()
		{
			return this.id;
		}
	}

	enum redrawRate
	{
		FASTEST("Fastest", 2),
		STANDARD("Standard", 4),
		SLOWER("Slower", 8),
		SLOWEST("Slowest", 16)
		;

		private String value;
		private int id;

		redrawRate(String value, int id)
		{
			this.value = value;
			this.id = id;
		}

		@Override
		public String toString()
		{
			return this.value;
		}

		public int toInt()
		{
			return this.id;
		}
	}

	enum targetSize
	{
		SMALL("320 x 180", 320, 180),
		MEDIUM("480 x 270", 480, 270),
		LARGE("640 x 360", 640, 360),
		XLARGE("800 x 450", 800, 450)
		;

		private String value;
		private int width;
		private int height;

		targetSize(String value, int width, int height)
		{
			this.value = value;
			this.width = width;
			this.height = height;
		}

		@Override
		public String toString()
		{
			return this.value;
		}
		public int getHeight()
		{
			return this.height;
		}
		public int getWidth()
		{
			return this.width;
		}
	}

	enum limitedDimension
	{
		HEIGHT("Height"),
		WIDTH("Width")
		;

		private String value;

		limitedDimension(String value)
		{
			this.value = value;
		}

		@Override
		public String toString()
		{
			return this.value;
		}

	}

	enum clickAction
	{
		REQUEST("Request Focus", 0),
		FORCE("Force Focus", 1),
		NOTHING("Do Nothing", 2)
		;

		private String value;
		private int action;

		clickAction(String value, int action)
		{
			this.action = action;
			this.value = value;
		}

		@Override
		public String toString()
		{
			return this.value;
		}
		public int clickMode()
		{
			return this.action;
		}
	}

	enum renderQuality
	{
		LOW("Low", RenderingHints.VALUE_RENDER_SPEED, RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR, 1),
		MEDIUM("Medium", RenderingHints.VALUE_RENDER_QUALITY, RenderingHints.VALUE_INTERPOLATION_BILINEAR, 1),
		HIGH("High", RenderingHints.VALUE_RENDER_QUALITY, RenderingHints.VALUE_INTERPOLATION_BILINEAR, 2)
		;

		private String value;
		private Object quality;
		private Object hint;
		private int redraw;

		renderQuality(String value, Object quality, Object hint, int redraw)
		{
			this.value = value;
			this.quality = quality;
			this.hint = hint;
			this.redraw = redraw;
		}

		@Override
		public String toString()
		{
			return this.value;
		}
		public Object getQuality()
		{
			return this.quality;
		}
		public Object getHint()
		{
			return this.hint;
		}
		public int getRedraw()
		{
			return this.redraw;
		}

	}

	enum barType
	{
		HITPOINTS("Hitpoints", Skill.HITPOINTS),
		PRAYER("Prayer", Skill.PRAYER),
		NONE("Disabled", null)
		;

		private String value;
		private Skill skill;

		barType(String value, Skill skill)
		{
			this.value = value;
			this.skill = skill;
		}

		@Override
		public String toString()
		{
			return this.value;
		}
		public Skill getSkill()
		{
			return this.skill;
		}
	}

	enum barPosition
	{
		BEFORE("Before", 0),
		AFTER("After", 1),
		OUTSIDE("Outside", 2)
		;

		private String value;
		private int position;

		barPosition(String value, int position)
		{
			this.value = value;
			this.position = position;
		}

		@Override
		public String toString()
		{
			return this.value;
		}
		public int getPosition()
		{
			return this.position;
		}
	}

	// =========== Picture in Picture Settings Section ===========

	@ConfigSection(
			name = "Picture in Picture Settings",
			description = "Size, quality, and position settings for the picture in picture window",
			position = 0
	)
	String pictureInPictureSettings = "pictureInPictureSettings";

	@ConfigItem(
			keyName = "quadrantID",
			name = "Position",
			description = "Configures the position of the Picture in Picture",
			position = 0,
			section = pictureInPictureSettings
	)
	default quadrant quadrantID() { return quadrant.BOTTOM_RIGHT; }

	@ConfigItem(
			keyName = "redrawRate",
			name = "Redraw Rate",
			description = "Configures the redraw rate of the Picture in Picture",
			position = 1,
			section = pictureInPictureSettings
	)
	default redrawRate redrawRate() { return redrawRate.STANDARD; }

	@ConfigItem(
			keyName = "renderQuality",
			name = "Render Quality",
			description = "Configures the render quality of the Picture in Picture",
			position = 2,
			section = pictureInPictureSettings
	)
	default renderQuality renderQuality() { return renderQuality.MEDIUM; }

	@ConfigItem(
			keyName = "paddingX",
			name = "Horizontal Padding",
			description = "The horizontal padding (in pixels) from the left/right edge of the screen",
			position = 3,
			section = pictureInPictureSettings
	)
	default int paddingX() { return 40; }

	@ConfigItem(
			keyName = "paddingY",
			name = "Vertical Padding",
			description = "The vertical padding (in pixels) from the top/bottom edge of the screen",
			position = 4,
			section = pictureInPictureSettings
	)
	default int paddingY() { return 25; }

	@ConfigItem(
			keyName = "targetSize",
			name = "Target Size",
			description = "Specifies the target size of the Picture in Picture",
			position = 5,
			section = pictureInPictureSettings
	)
	default targetSize targetSize() { return targetSize.MEDIUM; }

	@ConfigItem(
			keyName = "limitedDimension",
			name = "Limited Dimension",
			description = "Configures which dimension is limited when not 16:9",
			position = 6,
			section = pictureInPictureSettings
	)
	default limitedDimension limitedDimension() { return limitedDimension.HEIGHT; }

	@ConfigItem(
			keyName = "clickAction",
			name = "Click Action",
			description = "Action to perform when the Picture in Picture is clicked",
			position = 7,
			section = pictureInPictureSettings
	)
	default clickAction clickAction() { return clickAction.REQUEST; }


	// =========== Status Bar Section ===========

	@ConfigSection(
			name = "Status Bars",
			description = "Status Bar settings",
			position = 1
	)
	String statusBarSettings = "statusBarSettings";

	@ConfigItem(
			keyName = "leftBar",
			name = "Left Bar",
			description = "Configures the left status bar",
			position = 0,
			section = statusBarSettings
	)
	default barType leftBar() { return barType.HITPOINTS; }

	@ConfigItem(
			keyName = "rightBar",
			name = "Right Bar",
			description = "Configures the right status bar",
			position = 1,
			section = statusBarSettings
	)
	default barType rightBar() { return barType.PRAYER; }

	@ConfigItem(
			keyName = "barPosition",
			name = "Bar Position",
			description = "Sets the position of the status bars relative to the picture in picture",
			position = 2,
			section = statusBarSettings
	)
	default barPosition barPosition() { return barPosition.OUTSIDE; }

	@ConfigItem(
			keyName = "barText",
			name = "Show Counters",
			description = "Shows current value of the status on the bar",
			position = 3,
			section = statusBarSettings
	)
	default boolean barText() { return true; }

}
