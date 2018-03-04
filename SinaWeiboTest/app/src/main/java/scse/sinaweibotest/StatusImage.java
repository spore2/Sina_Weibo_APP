package scse.sinaweibotest;
//定义这个类是因为ImageAdapter要用，理论上不定义也可以
public class StatusImage {
    private int width;
    private int height;
    private String src;

    public StatusImage(String src) {
    //    this.width = width;
      //  this.height = height;
        this.src = src;
    }

    public int getWidth() {
        return width;
    }

    public void setWidth(int width) {
        this.width = width;
    }

    public int getHeight() {
        return height;
    }

    public void setHeight(int height) {
        this.height = height;
    }

    public String getSrc() {
        return src;
    }

    public void setSrc(String src) {
        this.src = src;
    }

}
