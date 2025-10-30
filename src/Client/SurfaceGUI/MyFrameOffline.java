package Client.SurfaceGUI;

import Client.Advance.ImageButton;
import Client.Advance.Music;
import Client.Character.Character;
import Client.SurfaceGUI.BeginGUI;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

/**
 * 单机模式游戏主界面
 * 继承JFrame，实现人机对战
 */
public class MyFrameOffline extends JFrame {
    private Character playerFighter;//玩家控制的角色
    private Character aiFighter;//AI控制的角色
    private AIController aiController;//AI控制器
    private ImageButton replay;//重玩按钮
    private ImageButton quit;//退出按钮
    
    private Surface surface;//画板
    private Thread paintThread;//刷新界面线程
    
    private boolean isFinish;//游戏是否结束

    public MyFrameOffline(int playerNumber) {
        super("GAME OFFLINE");
        
        // 设置玩家角色和AI角色
        Character fighter1 = new Character("fighter1",true,"images/cao",100,150);
        Character fighter2 = new Character("fighter2", false,"images/Chris",650,150);
        
        if (playerNumber == 1) {
            playerFighter = fighter1;
            aiFighter = fighter2;
        } else {
            playerFighter = fighter2;
            aiFighter = fighter1;
        }
        
        // 设置AI移动速度为较慢的值（玩家速度的一半）
        aiFighter.setSPEED(5);
        
        // 初始化AI控制器
        aiController = new AIController(aiFighter, playerFighter);
        
        launchFrame();
        
        // 设置窗口属性
        setSize(840, 520);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setResizable(false);
        setVisible(true);
        
        // 启动AI控制线程
        new Thread(aiController).start();
    }
    
    public void launchFrame() {
        // 添加画板
        surface = new Surface();
        this.addKeyListener(new KeyListener());
        this.add(surface);
        
        surface.setDoubleBuffered(true);//开启双缓冲
        
        // 添加重玩按钮
        replay = new ImageButton("/images/component/replayBt.png", 2.0/5,2.0/5);
        replay.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // 重新开始单机游戏
                dispose();
                MyFrameOffline offlineFrame = new MyFrameOffline(1);
            }
        });
        
        // 添加返回菜单按钮
        quit = new ImageButton("/images/component/quitBt.png", 2.0/5,2.0/5);
        quit.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // 返回主菜单
                dispose();
                BeginGUI beginGUI = new BeginGUI();
            }
        });
        
        surface.setLayout(new BorderLayout());
        JPanel panel = new JPanel();
        panel.setOpaque(false);
        panel.add(replay);
        panel.add(quit);
        
        // 一开始不可见
        replay.setVisible(false);
        quit.setVisible(false);
        
        surface.add(panel,BorderLayout.SOUTH);
        
        // 启动刷新线程
        paintThread = new Thread(new PaintThread());
        paintThread.start();
    }
    
    // 单机模式的PaintThread
     class PaintThread implements Runnable {
         public void run() {
             while(true) {
                 surface.repaint();
                 try {
                     Thread.sleep(30);//设备帧数：15 -> 60FPS
                 } catch (InterruptedException e) {
                     e.printStackTrace();
                     break;
                 }
             }
         }
     }
     
     // 单机模式的键盘监听器
     class KeyListener extends KeyAdapter {
         @Override
         public void keyPressed(KeyEvent e) {
             getKeyPressed(playerFighter, e.getKeyCode());
         }
         
         @Override
         public void keyReleased(KeyEvent e) {
             getKeyReleased(playerFighter, e.getKeyCode());
         }
     }
    
    // 单机模式的Surface类
    class Surface extends JPanel {
        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);

            //加载背景 - 适应窗口大小
            Toolkit tk =Toolkit.getDefaultToolkit();
            Image backGround = tk.getImage(Character.class.getClassLoader().getResource("images/background.png"));//加载背景
            g.drawImage(backGround,0,0,getWidth(),getHeight(),null);

            //加载状态栏 - 左右两侧各一个
            Image status = tk.getImage(Character.class.getClassLoader().getResource("images/component/blood/status.png"));
            int statusWidth = status.getWidth(null) / 5 * 2;
            int statusHeight = status.getHeight(null) / 5 * 2;
            
            // 左侧状态栏（玩家）
            g.drawImage(status,40,0,statusWidth,statusHeight,null);
            

            if(playerFighter.getHP() > 0 && aiFighter.getHP() > 0) {
                //加载血条 - 分别绘制在左右两侧状态栏上方
                Image p1HP = tk.getImage(Character.class.getClassLoader().getResource("images/component/blood/P1/" + playerFighter.getHP() + ".png"));
                Image p2HP = tk.getImage(Character.class.getClassLoader().getResource("images/component/blood/P2/"+ aiFighter.getHP() +".png"));
                
                // 玩家血条绘制在左侧状态栏上方
                g.drawImage(p1HP,40,0,statusWidth,statusHeight,null);
                
                // AI血条绘制在右侧状态栏上方
                g.drawImage(p2HP,getWidth() - statusWidth - 40,0,statusWidth,statusHeight,null);

                //调整视角，处理单机模式下的角色移动
                if(aiFighter.getPosition().y < playerFighter.getPosition().y) {
                    // AI在上方，先绘制AI
                    aiFighter.getDir().move(g, aiFighter);
                    playerFighter.getDir().move(g, playerFighter);
                } else {
                    // 玩家在上方，先绘制玩家
                    playerFighter.getDir().move(g, playerFighter);
                    aiFighter.getDir().move(g, aiFighter);
                }
            }
            
            //判断输赢
            else if(aiFighter.getHP() <= 0 && playerFighter.getHP() > 0) {
                //玩家胜利
                Image victory = tk.getImage(Character.class.getClassLoader().getResource("images/victory.png"));
                int victoryWidth = victory.getWidth(null) / 5 * 4;
                int victoryHeight = victory.getHeight(null) / 5 * 4;
                g.drawImage(victory,(getWidth() - victoryWidth) / 2,50,victoryWidth,victoryHeight,null);
                
                // 显示重玩和退出按钮
                replay.setVisible(true);
                quit.setVisible(true);
            } else if(playerFighter.getHP() <= 0 && aiFighter.getHP() > 0) {
                //AI胜利
                Image defeat = tk.getImage(Character.class.getClassLoader().getResource("images/defeat.png"));
                int defeatWidth = defeat.getWidth(null) / 3 * 2;
                int defeatHeight = defeat.getHeight(null) / 3 * 2;
                g.drawImage(defeat,(getWidth() - defeatWidth) / 2,50,defeatWidth,defeatHeight,null);
                
                // 显示重玩和退出按钮
                replay.setVisible(true);
                quit.setVisible(true);
            }
        }
    }
    
    public void getKeyPressed(Character fighter, int keyCode) {
        // 只处理玩家控制的角色
        if (fighter.equals(playerFighter)) {
            // 处理移动按键
            boolean currentDir = fighter.getDir().getCurrentDir();
            switch (keyCode) {
                case KeyEvent.VK_W:
                    if(currentDir == Character.LEFT) fighter.getDir().LU = true;
                    else fighter.getDir().RU = true;
                    break;
                case KeyEvent.VK_S:
                    if(currentDir == Character.LEFT) fighter.getDir().LD = true;
                    else fighter.getDir().RD = true;
                    break;
                case KeyEvent.VK_A:
                    fighter.getDir().setCurrentDir(Character.RIGHT);//保存当前方向
                    fighter.getDir().RF = true;
                    break;
                case KeyEvent.VK_D:
                    fighter.getDir().setCurrentDir(Character.LEFT);//保存当前方向
                    fighter.getDir().LF = true;
                    break;
                case KeyEvent.VK_J:
                    // 检查攻击冷却时间
                    if (!fighter.canAttack()) {
                        System.out.println("攻击冷却中，剩余时间：" + fighter.getRemainingCooldown() + "ms");
                        break;
                    }
                    
                    // 设置攻击时间，开始冷却
                    fighter.setAttackTime();
                    
                    // 延迟300ms，给予足够反应时间
                    Runnable task = new Runnable() {
                        @Override
                        public void run() {
                            fighter.getDir().A = true;
                            try {
                                Thread.sleep(300);
                            } catch (InterruptedException ex) {
                                ex.printStackTrace();
                            }
                            fighter.getDir().A = false;
                        }
                    };
                    Thread thread = new Thread(task);
                    thread.start();

                    // 检查攻击是否命中AI
                    if (Character.isAttacked(fighter, aiFighter)) {
                        aiFighter.setHP(aiFighter.getHP() - 1);
                        aiFighter.getDir().fighterFall();
                        System.out.println("玩家攻击AI成功！AI剩余血量：" + aiFighter.getHP());
                    }
                    break;
            }
            
            if(keyCode == KeyEvent.VK_W ||
                    keyCode == KeyEvent.VK_S ||keyCode == KeyEvent.VK_A ||keyCode == KeyEvent.VK_D ||keyCode == KeyEvent.VK_J) {
                fighter.getDir().LS = false;
                fighter.getDir().RS = false;
            }
        }
    }
    
    public void getKeyReleased(Character fighter, int keyCode) {
        // 只处理玩家控制的角色
        if (fighter.equals(playerFighter)) {
            switch (keyCode) {
                case KeyEvent.VK_W:
                    fighter.getDir().LU = false;
                    fighter.getDir().RU = false;
                    break;
                case KeyEvent.VK_S:
                    fighter.getDir().LD = false;
                    fighter.getDir().RD = false;
                    break;
                case KeyEvent.VK_A:
                    fighter.getDir().RF = false;
                    break;
                case KeyEvent.VK_D:
                    fighter.getDir().LF = false;
                    break;
            }
        }
    }
    
    // AI控制器类
    class AIController implements Runnable {
        private Character aiFighter;
        private Character playerFighter;
        private boolean running = true;
        
        public AIController(Character aiFighter, Character playerFighter) {
            this.aiFighter = aiFighter;
            this.playerFighter = playerFighter;
        }
        
        @Override
        public void run() {
            while (running) {
                try {
                    Thread.sleep(400); // AI决策间隔增加到400ms，进一步降低频率
                    makeAIDecision();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
        
        private void makeAIDecision() {
            // 获取双方位置
            Point aiPos = aiFighter.getPosition();
            Point playerPos = playerFighter.getPosition();
            
            // 计算距离
            int distance = Math.abs(aiPos.x - playerPos.x);
            
            // AI决策逻辑
            if (distance > 200) {
                // 距离较远，向玩家移动
                moveTowardsPlayer();
            } else if (distance > 80) {
                // 中等距离，较低概率攻击
                if (Math.random() < 0.15) {
                    // 15%概率攻击
                    performAttack();
                } else {
                    // 85%概率移动
                    moveRandomly();
                }
            } else {
                // 近距离，中等概率攻击
                if (Math.random() < 0.4) {
                    performAttack();
                } else {
                    moveRandomly();
                }
            }
        }
        
        private void moveTowardsPlayer() {
            Point aiPos = aiFighter.getPosition();
            Point playerPos = playerFighter.getPosition();
            
            // 先重置所有移动标志
            aiFighter.getDir().LF = false;
            aiFighter.getDir().RF = false;
            aiFighter.getDir().LU = false;
            aiFighter.getDir().LD = false;
            aiFighter.getDir().RU = false;
            aiFighter.getDir().RD = false;
            aiFighter.getDir().LS = false;
            aiFighter.getDir().RS = false;
            
            // 根据方向正确设置移动标志
            if (aiPos.x < playerPos.x) {
                // 玩家在右边，AI向右移动
                aiFighter.getDir().setCurrentDir(Character.LEFT);
                aiFighter.getDir().LF = true;  // 向右移动
            } else {
                // 玩家在左边，AI向左移动
                aiFighter.getDir().setCurrentDir(Character.RIGHT);
                aiFighter.getDir().RF = true;  // 向左移动
            }
            
            // 随机垂直移动
            if (Math.random() < 0.3) {
                if (Math.random() < 0.5) {
                    // 向上移动
                    if (aiFighter.getDir().getCurrentDir() == Character.LEFT) {
                        aiFighter.getDir().LU = true;
                    } else {
                        aiFighter.getDir().RU = true;
                    }
                } else {
                    // 向下移动
                    if (aiFighter.getDir().getCurrentDir() == Character.LEFT) {
                        aiFighter.getDir().LD = true;
                    } else {
                        aiFighter.getDir().RD = true;
                    }
                }
            }
        }
        
        private void moveRandomly() {
            // 先重置所有移动标志
            aiFighter.getDir().LF = false;
            aiFighter.getDir().RF = false;
            aiFighter.getDir().LU = false;
            aiFighter.getDir().LD = false;
            aiFighter.getDir().RU = false;
            aiFighter.getDir().RD = false;
            aiFighter.getDir().LS = false;
            aiFighter.getDir().RS = false;
            
            // 随机选择移动方向
            double rand = Math.random();
            if (rand < 0.25) {
                // 向右移动
                aiFighter.getDir().setCurrentDir(Character.LEFT);
                aiFighter.getDir().LF = true;
            } else if (rand < 0.5) {
                // 向左移动
                aiFighter.getDir().setCurrentDir(Character.RIGHT);
                aiFighter.getDir().RF = true;
            } else if (rand < 0.75) {
                // 向上移动
                if (aiFighter.getDir().getCurrentDir() == Character.LEFT) {
                    aiFighter.getDir().LU = true;
                } else {
                    aiFighter.getDir().RU = true;
                }
            } else {
                // 向下移动
                if (aiFighter.getDir().getCurrentDir() == Character.LEFT) {
                    aiFighter.getDir().LD = true;
                } else {
                    aiFighter.getDir().RD = true;
                }
            }
        }
        
        private void performAttack() {
            // 检查攻击冷却时间
            if (!aiFighter.canAttack()) {
                System.out.println("AI攻击冷却中");
                return;
            }
            
            // 设置攻击时间，开始冷却
            aiFighter.setAttackTime();
            
            // 执行攻击
            aiFighter.getDir().A = true;
            System.out.println("AI开始攻击");
            
            // 检查攻击是否命中
            if (Character.isAttacked(aiFighter, playerFighter)) {
                playerFighter.setHP(playerFighter.getHP() - 1);
                playerFighter.getDir().fighterFall();
                System.out.println("AI攻击玩家成功！玩家剩余血量：" + playerFighter.getHP());
            } else {
                System.out.println("AI攻击未命中");
            }
            
            // 攻击后重置状态
            try {
                Thread.sleep(300);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            aiFighter.getDir().A = false;
        }
        
        public void stop() {
            running = false;
        }
    }
    
    @Override
    public void dispose() {
        if (aiController != null) {
            aiController.stop();
        }
        super.dispose();
    }
}