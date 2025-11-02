package Client.Character;

import java.awt.*;

import java.util.ArrayList;


public class Character {
    public static final boolean LEFT = true;
    public static final boolean RIGHT = false;

    private String name;//角色名字

    private int HP = 5;//血量

    private  ArrayList<Image> movements;//人物角色的动作：左右停

    private int SPEED = 10;//当前移动速度（放大人物后适当提高速度）

    private Point position;//当前位置

    private Dir dir;//动作按钮类
    
    private Hitbox hitbox; // 碰撞箱
    private AttackBox leftAttackBox; // 向左攻击箱
    private AttackBox rightAttackBox;// 向右攻击箱
    private AttackBox leftKickBox;
    private AttackBox rightKickBox;
    
    private long lastAttackTime = 0;//上次攻击时间
    private long lastKickTime = 0;
    private static final long ATTACK_COOLDOWN = 500;//攻击冷却时间（毫秒）
    private static final long KICK_COOLDOWN = 1000;


    public Character(String name,boolean leftOrRight,String rootDir,int x,int y) {
        Toolkit tk =Toolkit.getDefaultToolkit();
        this.name = name;

        movements = new ArrayList<Image>();
        position = new Point(x,y);


        for(int i = 1; i <= 10; i++) {
            movements.add(tk.getImage(Character.class.getClassLoader()
                    .getResource(rootDir + "/" + i + ".gif")));
        }

        dir = new Dir(leftOrRight);//监控动作
        dir.setCurrentDir(leftOrRight);
        dir.createMap(movements);//创建对应动作maps
        
        // 初始化碰撞箱（使用边距参数）
        // 这些值需要根据实际图片调整，暂时使用示例值
        this.hitbox = new Hitbox(this, 10, 100, 10, 150);
        
        // 初始化攻击箱
        this.leftAttackBox = new AttackBox(this, "LA");
        this.rightAttackBox = new AttackBox(this, "RA");
        this.leftKickBox = new AttackBox(this,"LK");
        this.rightKickBox = new AttackBox(this,"RK");

    }//将图片载入缓存区, 并且做好索引


    //将当前动作画上面板（可以重写调整每一帧动作的图片位置）
    protected void drawCurrentMovement(Graphics g) {
        // 先更新方向再获取当前动作
        dir.locateDirection();//更新目前的动作
        Image currentMovement = dir.getCurrentMovement();
        
        // 添加null检查避免空指针异常
        if (currentMovement != null) {
            // 更新碰撞箱位置
            hitbox.updatePosition();
            
            // 更新攻击箱位置
            leftAttackBox.updatePosition();
            rightAttackBox.updatePosition();
            leftKickBox.updatePosition();
            rightKickBox.updatePosition();
            
            g.drawImage(currentMovement,
                    (int)position.getX(),
                    (int)position.getY(),
                    currentMovement.getWidth(null) * 2,
                    currentMovement.getHeight(null) * 2,null);
        }

    }

    /**
     * 当fighter1成功攻击fighter2时，返回true，否则返回false
     * @param fighter1 攻击者
     * @param fighter2 被攻击者
     * @return 是否攻击成功
     */
    public static boolean isAttacked(Character fighter1,Character fighter2) {
        // 如果被攻击者处于击倒状态，则无法再次被攻击
        if(fighter2.getDir().FALL) {
            System.out.println("攻击检测: 被攻击者处于FALL状态，无法被攻击");
            return false;
        }
        
        // 根据攻击者的方向选择对应的攻击箱进行检测
        AttackBox attackBox = null;
        if (fighter1.getDir().getCurrentDir() == Character.RIGHT) {
            // 使用向左攻击箱
            attackBox = fighter1.getLeftAttackBox();
        } else {
            // 使用向右攻击箱
            attackBox = fighter1.getRightAttackBox();
        }
        
        // 检查攻击箱是否与被攻击者的碰撞箱相交
        boolean isHit = attackBox.intersects(fighter2.getHitbox());
        
        if (isHit) {
            Point p1 = fighter1.getPosition();
            Point p2 = fighter2.getPosition();
            System.out.println("attack! 攻击者位置: (" + p1.x + ", " + p1.y + "), 被攻击者位置: (" + p2.x + ", " + p2.y + ") - 攻击成功！");
            return true;
        } else {
            Point p1 = fighter1.getPosition();
            Point p2 = fighter2.getPosition();
            System.out.println("攻击检测: 攻击者位置: (" + p1.x + ", " + p1.y + "), 被攻击者位置: (" + p2.x + ", " + p2.y + ") - 攻击失败");
        }
        
        return false;
    }

    public static boolean isKicked(Character fighter1,Character fighter2){
        if(fighter2.getDir().FALL) {
            System.out.println("踢腿检测: 被攻击者处于FALL状态，无法被踢中");
            return false;
        }
        AttackBox kickBox = null;
        if (fighter1.getDir().getCurrentDir() == Character.RIGHT) {
            // 使用左腿攻击箱
            kickBox = fighter1.leftKickBox;
        } else {
            // 使用右腿攻击箱
            kickBox = fighter1.rightKickBox;
        }
        boolean isHit = kickBox.intersects(fighter2.getHitbox());
        if (isHit) {
            Point p1 = fighter1.getPosition();
            Point p2 = fighter2.getPosition();
            System.out.println("kick! 攻击者位置: (" + p1.x + ", " + p1.y + "), 被攻击者位置: (" + p2.x + ", " + p2.y + ") - 踢腿成功！");
            return true;
        } else {
            Point p1 = fighter1.getPosition();
            Point p2 = fighter2.getPosition();
            System.out.println("踢腿检测: 攻击者位置: (" + p1.x + ", " + p1.y + "), 被攻击者位置: (" + p2.x + ", " + p2.y + ") - 踢腿失败");
        }
        return false;
    }
    
    /**
     * 防止两个角色重叠
     * @param character1 角色1
     * @param character2 角色2
     */
    public static void preventOverlap(Character character1, Character character2) {
        // 检查两个角色的碰撞箱是否相交
        if (character1.getHitbox().intersects(character2.getHitbox())) {
            // 获取两个角色的位置
            Point pos1 = character1.getPosition();
            Point pos2 = character2.getPosition();
            
            // 计算位置差异
            int dx = pos1.x - pos2.x;
            int dy = pos1.y - pos2.y;
            
            // 确定分离方向（优先水平分离）
            if (Math.abs(dx) > Math.abs(dy)) {
                // 水平分离
                if (dx > 0) {
                    // character1在右侧，将character1向右移动，character2向左移动
                    pos1.x += 5;
                    pos2.x -= 5;
                } else {
                    // character1在左侧，将character1向左移动，character2向右移动
                    pos1.x -= 5;
                    pos2.x += 5;
                }
            } else {
                // 垂直分离
                if (dy > 0) {
                    // character1在下方，将character1向下移动，character2向上移动
                    pos1.y += 5;
                    pos2.y -= 5;
                } else {
                    // character1在上方，将character1向上移动，character2向下移动
                    pos1.y -= 5;
                    pos2.y += 5;
                }
            }
            
            // 确保角色不会移出边界
            pos1.x = Math.max(0, Math.min(730, pos1.x));
            pos2.x = Math.max(0, Math.min(730, pos2.x));
            pos1.y = Math.max(120, Math.min(260, pos1.y));
            pos2.y = Math.max(120, Math.min(260, pos2.y));
        }
    }

    public Dir getDir() {
        return dir;
    }
    public Point getPosition() {
        return position;
    }
    public Hitbox getHitbox() {
        return hitbox;
    }
    public AttackBox getLeftAttackBox() {
        return leftAttackBox;
    }
    public AttackBox getRightAttackBox() {
        return rightAttackBox;
    }
    public AttackBox getLeftKickBox() {
        return leftKickBox;
    }
    public AttackBox getRightKickBox() {
        return rightKickBox;
    }
    public int getSPEED() {
        return SPEED;
    }
    public int getHP() {
        return HP;
    }
    public void setHP(int HP) {
        this.HP = HP;
    }
    
    /**
     * 设置移动速度
     * @param speed 新的移动速度
     */
    public void setSPEED(int speed) {
        this.SPEED = speed;
    }
    
    /**
     * 检查是否可以攻击（冷却时间是否结束）
     * @return 是否可以攻击
     */
    public boolean canAttack() {
        return System.currentTimeMillis() - lastAttackTime >= ATTACK_COOLDOWN;
    }
    
    /**
     * 设置攻击时间，开始冷却
     */
    public void setAttackTime() {
        lastAttackTime = System.currentTimeMillis();
    }
    
    /**
     * 获取剩余冷却时间
     * @return 剩余冷却时间（毫秒）
     */
    public long getRemainingCooldown() {
        long elapsed = System.currentTimeMillis() - lastAttackTime;
        return Math.max(0, ATTACK_COOLDOWN - elapsed);
    }

    public boolean canKick(){
        return System.currentTimeMillis() - lastKickTime >= KICK_COOLDOWN;
    }
    public void setKickTime(){
        lastKickTime = System.currentTimeMillis();
    }
    public long getKickRemainingCooldown(){
        long elapsed = System.currentTimeMillis() - lastKickTime;
        return Math.max(0, KICK_COOLDOWN - elapsed);
    }
}

