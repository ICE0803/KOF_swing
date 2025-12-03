package Client.Character;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Rectangle;

/**
 * 碰撞箱类，用于处理角色的碰撞检测
 */
public class Hitbox {
    private Character character; // 关联的角色
    private int leftMargin;   // 左边距
    private int rightMargin;  // 右边距
    private int topMargin;    // 上边距
    private int bottomMargin; // 下边距
    
    /**
     * 2.5D 场景下的“纵向容差”
     * 当两个角色在 Y 方向（深度）上的中心点距离大于该值时，
     * 认为它们不在同一“跑道”上，不发生身体/攻击碰撞。
     */
    public static final int DEPTH_TOLERANCE = 40;
    
    /**
     * 构造函数
     * @param character 关联的角色
     * @param leftMargin 左边距
     * @param rightMargin 右边距
     * @param topMargin 上边距
     * @param bottomMargin 下边距
     */
    public Hitbox(Character character, int leftMargin, int rightMargin, int topMargin, int bottomMargin) {
        this.character = character;
        this.leftMargin = leftMargin;
        this.rightMargin = rightMargin;
        this.topMargin = topMargin;
        this.bottomMargin = bottomMargin;
    }

    /**
     * 更新碰撞箱位置，使其跟随角色移动
     */
    public void updatePosition() {
        // 碰撞箱位置会根据角色位置和边距动态计算，无需额外操作
    }

    /**
     * 检测与其他碰撞箱是否发生碰撞
     * @param other 另一个碰撞箱
     * @return 是否发生碰撞
     */
    public boolean intersects(Hitbox other) {
        Rectangle thisBounds = this.getBounds();
        Rectangle otherBounds = other.getBounds();
        
        // 2.5D 处理：如果纵向（Y）中心点相差过大，则认为不在同一“深度”，不发生碰撞
        double thisCenterY = thisBounds.getCenterY();
        double otherCenterY = otherBounds.getCenterY();
        if (Math.abs(thisCenterY - otherCenterY) > DEPTH_TOLERANCE) {
            return false;
        }
        
        return thisBounds.intersects(otherBounds);
    }
    
    /**
     * 获取碰撞箱的Rectangle对象
     * @return 碰撞箱边界
     */
    public Rectangle getBounds() {
        // 根据角色当前位置和边距计算碰撞箱边界
        int charX = (int)character.getPosition().getX();
        int charY = (int)character.getPosition().getY();
        
        // 计算碰撞箱的实际边界
        int x = charX + leftMargin;
        int y = charY + topMargin;
        int width = (charX + rightMargin) - (charX + leftMargin);
        int height = (charY + bottomMargin) - (charY + topMargin);
        
        return new Rectangle(x, y, width, height);
    }

    /**
     * 设置碰撞箱边距
     * @param leftMargin 左边距
     * @param rightMargin 右边距
     * @param topMargin 上边距
     * @param bottomMargin 下边距
     */
    public void setMargins(int leftMargin, int rightMargin, int topMargin, int bottomMargin) {
        this.leftMargin = leftMargin;
        this.rightMargin = rightMargin;
        this.topMargin = topMargin;
        this.bottomMargin = bottomMargin;
    }
    
    /**
     * 获取左边距
     * @return 左边距
     */
    public int getLeftMargin() {
        return leftMargin;
    }
    
    /**
     * 获取右边距
     * @return 右边距
     */
    public int getRightMargin() {
        return rightMargin;
    }
    
    /**
     * 获取上边距
     * @return 上边距
     */
    public int getTopMargin() {
        return topMargin;
    }
    
    /**
     * 获取下边距
     * @return 下边距
     */
    public int getBottomMargin() {
        return bottomMargin;
    }

    /**
     * 获取碰撞箱中心点X坐标
     * @return 中心点X坐标
     */
    public int getCenterX() {
        Rectangle bounds = getBounds();
        return (int)bounds.getCenterX();
    }
    
    /**
     * 获取碰撞箱中心点Y坐标
     * @return 中心点Y坐标
     */
    public int getCenterY() {
        Rectangle bounds = getBounds();
        return (int)bounds.getCenterY();
    }
    
    /**
     * 绘制碰撞箱边框（用于调试）
     * @param g Graphics对象
     */
    public void drawBounds(Graphics g) {
        Rectangle bounds = getBounds();
        g.setColor(Color.RED);
        g.drawRect(bounds.x, bounds.y, bounds.width, bounds.height);
    }
}