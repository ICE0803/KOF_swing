package Client.Character;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Rectangle;

/**
 * 攻击箱类，用于处理角色攻击动作的攻击检测
 */
public class AttackBox {
    private Rectangle bounds; // 攻击箱边界
    private Character character; // 关联的角色
    private String attackType; // 攻击类型("LA"或"RA")

    public static final int DEPTH_TOLERANCE = 100;
    
    /**
     * 构造函数
     * @param character 关联的角色
     * @param attackType 攻击类型("LA"或"RA")
     */
    public AttackBox(Character character, String attackType) {
        this.character = character;
        this.attackType = attackType;
        // 初始化攻击箱，位置与角色位置一致，但大小根据攻击类型调整
        this.bounds = new Rectangle(0, 0, 0, 0);
        updatePosition();
    }
    
    /**
     * 更新攻击箱位置和大小，使其跟随角色移动和攻击动作
     */
    public void updatePosition() {
        // 根据角色当前的位置和攻击类型更新攻击箱位置
        int charX = (int)character.getPosition().getX();
        int charY = (int)character.getPosition().getY();
        int charWidth = 60; // 角色图片宽度
        int charHeight = 80; // 角色图片高度
        
        // 根据攻击类型设置不同的攻击箱位置和大小
        if ("LA".equals(attackType)) {
            // 向左攻击时，攻击箱在角色的左侧
            bounds.setBounds(
                charX - 20,  // X坐标：角色左侧
                charY + 20,  // Y坐标：角色中间偏下
                40,          // 宽度
                30           // 高度
            );
        } else if ("RA".equals(attackType)) {
            // 向右攻击时，攻击箱在角色的右侧
            bounds.setBounds(
                charX + 100,  // X坐标：角色右侧
                charY + 20,  // Y坐标：角色中间偏下
                40,          // 宽度
                30           // 高度
            );
        } else if ("LK".equals(attackType)) {
            // 向左踢腿时，攻击箱在角色的左侧下方
            bounds.setBounds(
                charX - 20,  // X坐标：角色左侧下方
                charY + 120,  // Y坐标：角色腿部位置
                40,          // 宽度
                30           // 高度
            );
        } else if ("RK".equals(attackType)) {
            // 向右踢腿时，攻击箱在角色的右侧下方
            bounds.setBounds(
                charX + 100,  // X坐标：角色右侧下方
                charY + 120,  // Y坐标：角色腿部位置
                40,          // 宽度
                30           // 高度
            );
        } else {
            // 默认情况下，攻击箱与角色碰撞箱相同
            bounds.setBounds(charX, charY, charWidth, charHeight);
        }
    }
    
    /**
     * 检测与其他碰撞箱是否发生攻击碰撞
     * @param other 另一个碰撞箱
     * @return 是否发生攻击碰撞
     */
    public boolean intersects(Hitbox other) {
        Rectangle otherBounds = other.getBounds();
        
        // 2.5D 处理：如果纵向（Y）中心点相差过大，则认为不在同一“深度”，不发生攻击碰撞
        double thisCenterY = bounds.getCenterY();
        double otherCenterY = otherBounds.getCenterY();
        if (Math.abs(thisCenterY - otherCenterY) > AttackBox.DEPTH_TOLERANCE) {
            return false;
        }
        
        return this.bounds.intersects(otherBounds);
    }
    
    /**
     * 获取攻击箱的Rectangle对象
     * @return 攻击箱边界
     */
    public Rectangle getBounds() {
        return bounds;
    }
    
    /**
     * 获取攻击类型
     * @return 攻击类型
     */
    public String getAttackType() {
        return attackType;
    }
    
    /**
     * 绘制攻击箱边框（用于调试）
     * @param g Graphics对象
     */
    public void drawBounds(Graphics g) {
        g.setColor(Color.BLUE);
        g.drawRect(bounds.x, bounds.y, bounds.width, bounds.height);
    }
}