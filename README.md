# RobotBattle
## 游戏概要:

* 一个随机生成的N*N的地图；所有参赛机器人将初始化散布在地图的随机位置；地图上定期会随机在地图上生成血包和地雷；机器人之间可以互相攻击；

PS：沙盒，按‘S’键，开始对战；按‘R’键重置；游戏结束按‘回车’键查看积分排名；Up键提速；down键减速


## 详细规则
### 关于地图
* 一个随机生成的N*N的网格地图
* 初始化时，地图上随机散布 ‘机器人数量’个血包；M（随地图大小变化）个地雷

### 关于机器人
*  游戏开始，机器人将初始化散布在地图的随机位置
*  机器人可以在地图边界内上下左右移动
*  机器人血量变更说明
*  机器人初始化时为10点血
*  机器人抵达血包位置，加1点血。（同时该血包消失）
*  机器人踩中地雷位置，减1点血（同时该地雷消失）
*  机器人被其它机器人踩中，减2点血

### 关于机器人大作战沙盒机制机制
* 回合制。每个回合，每个机器人只能移动1步（当机器人被踩中后，下一次可移动2步）
* 每50的整数倍回合开始时，地图生会随机生成散布血包和地雷
* 血包数量： 机器人数量
* 地雷数量： M（随地图大小，回合数变化）
* 结束条件：
    * 当场上只剩最后一个机器人时，则游戏结束
    * 当总回合数达到10000步时，则游戏结束
    
### 关于比分计算
* 分数 = （机器人数量 - 存活排名  + 1）* 5 + 剩余血量 + 获得血包数量 + 攻击数 * 2  - 地雷数 - 异常数 * 2


