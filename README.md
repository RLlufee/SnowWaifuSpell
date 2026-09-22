# Snow Waifu Spell

Have you ever felt a twinge of regret after defeating the **Snow Queen**—wishing she could accompany you, rather than becoming just another trophy head?  
This mod introduces a new spell: **Snowbound Oath**.

***

## 📜 How It Works

*   After defeating the Snow Queen, she will drop her **soul**.
*   Using this soul grants you a **random level 1–3 Snowbound Oath scroll**.
*   With the scroll, you can summon the **Snow Waifu** in her loyal servant form.

![](https://media.forgecdn.net/attachments/description/1320652/description_5758ba0d-2135-4077-b3df-a578ae89a932.png)

***

## ❄ Snow Waifu Features

*   The Snow Waifu will assist you in battle using a variety of **ice-based spells**.
*   **Right-click** her to toggle between standing and sitting (standby) mode.
*   **Sneak + Right-click** will **teleport her to your position** and set her to sit in place.
*   **Right-click with an empty bucket** for a **special easter egg**.

***

## ⚙ Configuration

You can customize the Snow Waifu’s stats (HP, damage, skill strength, summon duration, etc.) in: config/SnowWaifuSpellConfig.toml

***

Enjoy the company of your icy companion as she fights at your side!

***

## 📝 更新日志 (Changelog)

### [1.21.1-1.1.1-Qingi]
- **彩蛋特效增强**：
  - 玩家右键使用空桶给已驯服的冰雪女王挤奶时，冰雪女王将同步冒出爱心粒子特效（向周围所有客户端广播实体爱心效果事件）。
- **技能冷却动态配置接入**：
  - 冰锥术（`icicleInterval`）与霜冻射线（`iceRayInterval`）冷却机制接入 `SnowWaifuConfig` 配置读取，消除硬编码。

### [1.21.1-1.1.0-Qingi]
- **雪女施法准头与战斗 AI 全面重构移植**：
  - **冰锥术（IcicleProjectile）弹道与瞄准校准**：
    - 统一从雪女眼部下方发射（`getEyePosition().subtract(0, 0.2, 0)`），修正原脚底起射导致的仰角严重偏差。
    - 瞄准目标实体身体质心（`getBoundingBox().getCenter()`），移除对无重力弹射物的多余仰角偏移，散布设为 0，弹道指哪打哪。
    - 施法前加入 `hasLineOfSight(target)` 视线判定并强制锁定雪女视线与身体朝向。
  - **极寒喷雾（ConeOfColdProjectile）生成坐标与朝向修复**：
    - 修复 1.21.1 原先恶性坐标计算 Bug（`getEyeY() * 0.7 + getY()` 导致喷雾生成在天上几十格高空），修正为眼高偏下 0.8 格（与 ISS 底层 `AbstractConeProjectile` 规范对齐）。
    - 喷雾期间强行同步雪女身体朝向（`yRot`）至视线（`yHeadRot`），确保前方范围判定准确覆盖目标。
  - **霜冻射线（RayOfFrost）视线判定与穿模优化**：
    - 施法前加入 `hasLineOfSight` 检查，避免隔墙空射消耗冷却。
    - 射线目标对齐质心，并加入友军过滤（`!isAlliedTo(e)`），避免误伤主人其它宠物或召唤物。
  - **悬停与盘旋移动 AI（`NewHoverBeamGoal`）平滑重构**：
    - 消除每刻随机选点导致的急停、剧烈空中抖动与抽搐，采用平滑环绕方位角步进与 40 tick 选位冷却。
    - 盘旋与移动过程中持续锁定目标视线并校准身体朝向，为空战提供极其稳定的空对地打击平台。
- **召唤多雪女防内讧与友军伤害完全豁免**：
  - **同伴与友军伤害完全免疫**：在 `SummonedSnowQueen.hurt()` 中判定若受击来源为同伴雪女、主人或其他盟友实体，直接完全免疫伤害，杜绝因极寒喷雾或射线波及引发受击反击互殴。
  - **驯服状态与盟友判定闭环**：召唤初始化时显式设置 `setTame(true)`，并在 `isAlliedTo()` 中把同一主人的所有雪女及驯服生物视作盟友。
  - **目标选择与攻击意图闭环**：重写 `canAttack()` 与 `wantsToAttack()` 严禁锁定同伴；在 `QueenHurtByTargetGoal` 与 `QueenProtectOwnerTargetGoal` 中加入 `wantsToAttack()` 校验，从源头上杜绝呼叫同伴攻击同伴雪女的“姐妹掐架”现象。
- **配置项扩展与同步**：
  - 新增 `icicleDamage.a`、`icicleDamage.b` 冰锥伤害斜率与截距配置。
  - 新增 `icicleInterval` 冰锥施法间隔（默认 40 tick / 2 秒），兼容旧版 `snowBallInterval`。
  - 新增 `snowWaifuForever` 永久存在配置项（开启时不添加 Recast/SummonTimer，雪女永久陪伴主人）。
- **AI 目标选择器自建解耦**：
  - 弃用并移除了对 `io.redspace.ironsspellbooks.entity.mobs.goals.*` 的直接依赖，防止因 ISS（Iron's Spells 'n Spellbooks）跨版本变更内部类及构造签名导致游戏在注册或实例化时崩溃。
  - 新增独立的 AI 目标选择器：
    - `QueenOwnerHurtByTargetGoal`（主人受击反击）
    - `QueenOwnerHurtTargetGoal`（协同攻击主人目标）
    - `QueenCopyOwnerTargetGoal`（跟随生物主人的目标）
    - `QueenHurtByTargetGoal`（受击反击并支持呼叫同伴，同时忽略主人与盟友伤害）
    - `QueenProtectOwnerTargetGoal`（主动警戒并拦截锁定主人或盟友的敌对生物）
  - 保留并完善了坐下状态校验、友军/召唤物防内讧逻辑及伤害源过滤。
