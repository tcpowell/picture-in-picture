package com.pip;

import net.runelite.api.Skill;
import net.runelite.client.config.*;

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


	// =========== Size and Position ===========

	@ConfigSection(
			name = "Size and Position",
			description = "Size, quality, and position settings for the picture in picture window",
			position = 0
	)
	String sizeAndPosition = "sizeAndPosition";

	@ConfigItem(
			keyName = "quadrantID",
			name = "Position",
			description = "Configures the position of the Picture in Picture",
			position = 0,
			section = sizeAndPosition
	)
	default quadrant quadrantID() { return quadrant.BOTTOM_RIGHT; }

	@ConfigItem(
			keyName = "paddingX",
			name = "Horizontal Padding",
			description = "The horizontal padding (in pixels) from the left/right edge of the screen",
			position = 1,
			section = sizeAndPosition
	)
	default int paddingX() { return 40; }

	@ConfigItem(
			keyName = "paddingY",
			name = "Vertical Padding",
			description = "The vertical padding (in pixels) from the top/bottom edge of the screen",
			position = 2,
			section = sizeAndPosition
	)
	default int paddingY() { return 25; }

	@ConfigItem(
			keyName = "targetSize",
			name = "Target Size",
			description = "Specifies the target size of the Picture in Picture",
			position = 3,
			section = sizeAndPosition
	)
	default targetSize targetSize() { return targetSize.MEDIUM; }

	@ConfigItem(
			keyName = "limitedDimension",
			name = "Limited Dimension",
			description = "Configures which dimension is limited when not 16:9",
			position = 4,
			section = sizeAndPosition
	)
	default limitedDimension limitedDimension() { return limitedDimension.HEIGHT; }

	@ConfigItem(
			keyName = "borderWidth",
			name = "Border Width",
			description = "Sets the border width of the Picture in Picture window",
			position = 5,
			section = sizeAndPosition
	)
	@Range(
			min = 0,
			max = 20
	)
	default int borderWidth() { return 2; }

	@ConfigItem(
			keyName = "clickAction",
			name = "Click Action",
			description = "Action to perform when the Picture in Picture is clicked",
			position = 6,
			section = sizeAndPosition
	)
	default clickAction clickAction() { return clickAction.REQUEST; }


	// =========== Image Quality ===========

	@ConfigSection(
			name = "Image Quality",
			description = "Image Quality Settings",
			position = 1
	)
	String imageQuality = "imageQuality";

	@ConfigItem(
			keyName = "redrawRate",
			name = "Redraw Rate",
			description = "Configures the redraw rate of the Picture in Picture",
			position = 0,
			section = imageQuality
	)
	default redrawRate redrawRate() { return redrawRate.STANDARD; }

	@ConfigItem(
			keyName = "renderQuality",
			name = "Render Quality",
			description = "Configures the render quality of the Picture in Picture",
			position = 1,
			section = imageQuality
	)
	default renderQuality renderQuality() { return renderQuality.MEDIUM; }


	// =========== Status Bar Section ===========

	@ConfigSection(
			name = "Status Bars",
			description = "Status Bar settings",
			position = 2
	)
	String statusBars = "statusBars";

	@ConfigItem(
			keyName = "leftBar",
			name = "Left Bar",
			description = "Configures the left status bar",
			position = 0,
			section = statusBars
	)
	default barType leftBar() { return barType.HITPOINTS; }

	@ConfigItem(
			keyName = "rightBar",
			name = "Right Bar",
			description = "Configures the right status bar",
			position = 1,
			section = statusBars
	)
	default barType rightBar() { return barType.PRAYER; }

	@ConfigItem(
			keyName = "barPosition",
			name = "Bar Position",
			description = "Sets the position of the status bars relative to the picture in picture",
			position = 2,
			section = statusBars
	)
	default barPosition barPosition() { return barPosition.OUTSIDE; }

	@ConfigItem(
			position = 3,
			keyName = "barWidth",
			name = "Bar Width",
			description = "Specify the width of status bars (1-50)",
			section = statusBars
	)
	@Range(
			min = 1,
			max = 50
	)
	default int getBarWidth()
	{
		return 20;
	}

	@ConfigItem(
			keyName = "barText",
			name = "Show Bar Text",
			description = "Shows current value of the status on the bar (Bar Width >=15)",
			position = 4,
			section = statusBars
	)
	default boolean barText() { return true; }

}
