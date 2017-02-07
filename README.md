# HtmlView
Android Html富文本view,支持一些基本的标签。__开发中部分无法使用__

### 软件截图
![image](https://github.com/freedom10086/HtmlView/blob/master/art/img1.jpg)
![image](https://github.com/freedom10086/HtmlView/blob/master/art/img2.jpg)
 
- 替换了android `Html` 类的html解析器（应该说是xml解析器,他的做法是先将html转换为xml然后再解析）
- 全标签支持自定义,源码中常用标签无法自定义,自定义的`mTagHandler`无法处理如 `br` `p` `ul`...等标签
- 属性解析器自定义,自带的属性解析器只能获取到`color` `background-color` `text-align` `text-decoration`等属性

### 附`android.text.Html`类中无法自定义的标签
```java
private void handleStartTag(String tag, Attributes attributes) {
        if (tag.equalsIgnoreCase("br")) {
            // We don't need to handle this. TagSoup will ensure that there's a </br> for each <br>
            // so we can safely emit the linebreaks when we handle the close tag.
        } else if (tag.equalsIgnoreCase("p")) {
            startBlockElement(mSpannableStringBuilder, attributes, getMarginParagraph());
            startCssStyle(mSpannableStringBuilder, attributes);
        } else if (tag.equalsIgnoreCase("ul")) {
            startBlockElement(mSpannableStringBuilder, attributes, getMarginList());
        } else if (tag.equalsIgnoreCase("li")) {
            startLi(mSpannableStringBuilder, attributes);
        } else if (tag.equalsIgnoreCase("div")) {
            startBlockElement(mSpannableStringBuilder, attributes, getMarginDiv());
        } else if (tag.equalsIgnoreCase("span")) {
            startCssStyle(mSpannableStringBuilder, attributes);
        } else if (tag.equalsIgnoreCase("strong")) {
            start(mSpannableStringBuilder, new Bold());
        } else if (tag.equalsIgnoreCase("b")) {
            start(mSpannableStringBuilder, new Bold());
        } else if (tag.equalsIgnoreCase("em")) {
            start(mSpannableStringBuilder, new Italic());
        } else if (tag.equalsIgnoreCase("cite")) {
            start(mSpannableStringBuilder, new Italic());
        } else if (tag.equalsIgnoreCase("dfn")) {
            start(mSpannableStringBuilder, new Italic());
        } else if (tag.equalsIgnoreCase("i")) {
            start(mSpannableStringBuilder, new Italic());
        } else if (tag.equalsIgnoreCase("big")) {
            start(mSpannableStringBuilder, new Big());
        } else if (tag.equalsIgnoreCase("small")) {
            start(mSpannableStringBuilder, new Small());
        } else if (tag.equalsIgnoreCase("font")) {
            startFont(mSpannableStringBuilder, attributes);
        } else if (tag.equalsIgnoreCase("blockquote")) {
            startBlockquote(mSpannableStringBuilder, attributes);
        } else if (tag.equalsIgnoreCase("tt")) {
            start(mSpannableStringBuilder, new Monospace());
        } else if (tag.equalsIgnoreCase("a")) {
            startA(mSpannableStringBuilder, attributes);
        } else if (tag.equalsIgnoreCase("u")) {
            start(mSpannableStringBuilder, new Underline());
        } else if (tag.equalsIgnoreCase("del")) {
            start(mSpannableStringBuilder, new Strikethrough());
        } else if (tag.equalsIgnoreCase("s")) {
            start(mSpannableStringBuilder, new Strikethrough());
        } else if (tag.equalsIgnoreCase("strike")) {
            start(mSpannableStringBuilder, new Strikethrough());
        } else if (tag.equalsIgnoreCase("sup")) {
            start(mSpannableStringBuilder, new Super());
        } else if (tag.equalsIgnoreCase("sub")) {
            start(mSpannableStringBuilder, new Sub());
        } else if (tag.length() == 2 &&
                Character.toLowerCase(tag.charAt(0)) == 'h' &&
                tag.charAt(1) >= '1' && tag.charAt(1) <= '6') {
            startHeading(mSpannableStringBuilder, attributes, tag.charAt(1) - '1');
        } else if (tag.equalsIgnoreCase("img")) {
            startImg(mSpannableStringBuilder, attributes, mImageGetter);
        } else if (mTagHandler != null) {
            mTagHandler.handleTag(true, tag, mSpannableStringBuilder, mReader);
        }
    }
```
