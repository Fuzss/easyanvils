package fuzs.easyanvils.util;

import net.minecraft.ChatFormatting;
import net.minecraft.client.StringSplitter;
import net.minecraft.client.gui.Font;
import net.minecraft.network.chat.Style;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.util.FormattedCharSink;
import net.minecraft.util.Mth;
import org.apache.commons.lang3.mutable.MutableFloat;
import org.apache.commons.lang3.mutable.MutableInt;

import javax.annotation.Nullable;

/**
 * custom string decomposer for iterating formatted strings without removing formatting codes, intended for text fields
 */
public class FormattedStringHelper {

    public static int stringWidth(Font font, @Nullable String content) {
        if (content == null) {
            return 0;
        } else {
            MutableFloat mutableFloat = new MutableFloat();
            FormattedStringHelper.iterateFormatted(content, Style.EMPTY, (index, style, j) -> {
                mutableFloat.add(font.getSplitter().stringWidth(FormattedCharSequence.forward(Character.toString(j), style)));
                return true;
            });
            return Mth.ceil(mutableFloat.floatValue());
        }
    }

    public static boolean isAllowedChatCharacter(char character) {
        return character >= ' ' && character != 127;
    }

    public static String filterText(String input) {
        return filterText(input, false);
    }

    public static String filterText(String string, boolean keepLinesBreaks) {
        StringBuilder stringBuilder = new StringBuilder();
        char[] var3 = string.toCharArray();
        for (char c : var3) {
            if (isAllowedChatCharacter(c)) {
                stringBuilder.append(c);
            } else if (keepLinesBreaks && c == '\n') {
                stringBuilder.append(c);
            }
        }
        return stringBuilder.toString();
    }

    public static int plainIndexAtWidth(Font font, String content, int maxWidth, Style style) {
        WidthLimitedCharSink widthLimitedCharSink = new WidthLimitedCharSink(font.getSplitter(), (float) maxWidth);
        iterateFormatted(content, style, widthLimitedCharSink);
        return widthLimitedCharSink.getPosition();
    }

    public static String plainHeadByWidth(Font font, String content, int maxWidth, Style style) {
        return content.substring(0, plainIndexAtWidth(font, content, maxWidth, style));
    }

    public static String plainTailByWidth(Font font, String content, int maxWidth, Style style) {
        MutableFloat mutableFloat = new MutableFloat();
        MutableInt mutableInt = new MutableInt(content.length());
        iterateFormattedBackwards(content, style, (j, stylex, k) -> {
            float stringWidth = font.getSplitter().stringWidth(FormattedCharSequence.forward(Character.toString(k), stylex));
            float f = mutableFloat.addAndGet(stringWidth);
            if (f > maxWidth) {
                return false;
            } else {
                mutableInt.setValue(j);
                return true;
            }
        });
        return content.substring(mutableInt.intValue());
    }

    public static boolean iterateFormatted(String text, Style defaultStyle, FormattedCharSink sink) {
        int textLength = text.length();
        Style currentStyle = defaultStyle;

        for (int position = 0; position < textLength; ++position) {
            char character = text.charAt(position);
            if (character == '§') {
                if (position + 1 < textLength) {
                    char d = text.charAt(position + 1);
                    ChatFormatting chatFormatting = ChatFormatting.getByCode(d);
                    if (chatFormatting != null) {
                        currentStyle = chatFormatting == ChatFormatting.RESET ? defaultStyle : currentStyle.applyLegacyFormat(chatFormatting);
                    }

                    if (feedChar(text, defaultStyle, sink, position, character, textLength) == -1) {
                        return false;
                    }
                    if (feedChar(text, defaultStyle, sink, ++position, text.charAt(position), textLength) == -1) {
                        return false;
                    }
                } else {
                    return feedChar(text, defaultStyle, sink, position, character, textLength) != -1;
                }

            } else {
                position = feedChar(text, currentStyle, sink, position, character, textLength);
                if (position == -1) {
                    return false;
                }
            }
        }

        return true;
    }

    public static boolean iterateFormattedBackwards(String text, Style defaultStyle, FormattedCharSink sink) {
        int textLength = text.length();
        Style currentStyle = defaultStyle;

        for (int position = textLength - 1; position >= 0; --position) {
            char character = text.charAt(position);
            if (character == '§') {
                if (position - 1 >= 0) {
                    char d = text.charAt(position - 1);
                    ChatFormatting chatFormatting = ChatFormatting.getByCode(d);
                    if (chatFormatting != null) {
                        currentStyle = chatFormatting == ChatFormatting.RESET ? defaultStyle : currentStyle.applyLegacyFormat(chatFormatting);
                    }

                    if (feedChar(text, defaultStyle, sink, position, character, textLength, false) == -1) {
                        return false;
                    }
                    if (feedChar(text, defaultStyle, sink, --position, text.charAt(position), textLength, false) == -1) {
                        return false;
                    }
                } else {
                    return feedChar(text, defaultStyle, sink, position, character, textLength, false) != -1;
                }

            } else {
                position = feedChar(text, currentStyle, sink, position, character, textLength, false);
                if (position == -1) {
                    return false;
                }
            }
        }

        return true;
    }

    private static int feedChar(String text, Style style, FormattedCharSink sink, int position, char character, int textLength) {
        return feedChar(text, style, sink, position, character, textLength, true);
    }

    private static int feedChar(String text, Style style, FormattedCharSink sink, int position, char character, int textLength, boolean forwardIteration) {
        if (Character.isHighSurrogate(character)) {
            if (forwardIteration && position + 1 >= textLength || !forwardIteration && position - 1 < 0) {
                if (!sink.accept(position, style, 65533)) {
                    return -1;
                }
                return forwardIteration ? textLength : -2;
            }

            char d = text.charAt(position + (forwardIteration ? 1 : -1));
            if (Character.isLowSurrogate(d)) {
                if (!sink.accept(position, style, Character.toCodePoint(character, d))) {
                    return -1;
                }

                position += forwardIteration ? 1 : -1;
            } else if (!sink.accept(position, style, 65533)) {
                return -1;
            }
        } else if (!feedChar(style, sink, position, character)) {
            return -1;
        }
        return position;
    }

    private static boolean feedChar(Style style, FormattedCharSink sink, int position, char character) {
        return Character.isSurrogate(character) ? sink.accept(position, style, 65533) : sink.accept(position, style, character);
    }

    private static class WidthLimitedCharSink implements FormattedCharSink {
        private final StringSplitter splitter;
        private float maxWidth;
        private int position;

        public WidthLimitedCharSink(StringSplitter splitter, float f) {
            this.splitter = splitter;
            this.maxWidth = f;
        }

        @Override
        public boolean accept(int i, Style style, int j) {
            this.maxWidth -= this.splitter.stringWidth(FormattedCharSequence.forward(Character.toString(j), style));
            if (this.maxWidth >= 0.0F) {
                this.position = i + Character.charCount(j);
                return true;
            } else {
                return false;
            }
        }

        public int getPosition() {
            return this.position;
        }

        public void resetPosition() {
            this.position = 0;
        }
    }
}
