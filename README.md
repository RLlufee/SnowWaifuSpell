# Snow Waifu Spell Resumed | 雪下的誓言·再续

![](https://cdn.jsdelivr.net/gh/RLlufee/images@main/Assets/snow_wife.png)

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

### [1.20.1-1.1.1-Qingi]
- **彩蛋特效增强**：
  - 玩家右键使用空桶给已驯服的冰雪女王挤奶时，冰雪女王将同步冒出爱心粒子特效（向周围所有客户端广播实体爱心效果事件）。
- **技能冷却动态配置接入**：
  - 冰锥术（`icicleInterval`）与霜冻射线（`iceRayInterval`）冷却机制接入 `SnowWaifuConfig` 配置读取，消除硬编码。
- **ISS 跨版本实体注销兼容性修复（解决 NoSuchMethodError 崩溃）**：
  - 针对高版本 ISS（如 3.16+）在 `IMagicSummon` 中将 `onRemovedHelper` 参数由 `(Entity, SummonTimer)` 改动为 `(Entity, RegistryObject<MobEffect>)` 或 `(Entity)` 导致的跨版本二进制 ABI 不兼容抛出 `NoSuchMethodError` 崩溃问题，改为通过反射动态适配当前运行时环境，并提供独立的原生效果清理兜底逻辑，保证无论在新旧版本 ISS 下实体退场时均绝不崩溃。
- **召唤多雪女防内讧与友军互殴修复**：
  - **同伴与友军伤害完全豁免**：在 `SummonedSnowQueen.hurt()` 中判定若受击来源为同伴雪女、主人或其他盟友实体，直接免疫伤害，杜绝因极寒喷雾、冰锥或射线群体波及引发受击反击互殴。
  - **驯服状态与盟友关系判定完善**：在召唤初始化时正确显式赋予 `setTame(true)`，并在 `isAlliedTo()` 中明确将相同主人的所有雪女及驯服生物视作盟友。
  - **目标选择与攻击意图闭环**：重写 `canAttack()` 与 `wantsToAttack()` 严禁锁定雪女同伴；在 `QueenHurtByTargetGoal` 中加入 `wantsToAttack()` 校验，从源头上杜绝呼叫同伴攻击同伴雪女的“姐妹掐架”现象。
- **雪女施法准头与战斗 AI 全面重构优化**：
  - **冰锥术（IcicleProjectile）弹道与瞄准校准**：
    - 修复发射起点（头部）与方向向量（原先错用脚底坐标）错位导致的发射俯仰角严重偏差问题。
    - 移除对无重力直线飞行弹射物硬加的 `+0.1` 仰角补偿，彻底解决贴头皮飞过、“高射炮打蚊子”等落空问题。
    - 将目标瞄准点精准对齐至目标实体的身体质心（`getBoundingBox().getCenter()`），散布设为 0，大幅提升命中率。
  - **极寒喷雾（ConeOfColdProjectile）生成坐标与朝向修复**：
    - 修复原坐标计算手滑错误（原 `getEyeY() * 0.7 + getY()` 导致实体生成在天顶数十格高空）的严重恶性 Bug，修正为眼高偏下 0.8 格（与 ISS 底层 `AbstractConeProjectile` 完全契合）。
    - 喷雾施法期间强行同步雪女身体朝向（`yRot`）至头部视线（`yHeadRot`），确保前方锥形伤害判定准确朝向目标。
  - **霜冻射线（RayOfFrost）视线判定与穿模优化**：
    - 施法前加入 `hasLineOfSight` 视线检查，避免隔墙时把冷却浪费在射空气或撞击身前掩体上。
    - 射线目标改为瞄准质心，并增加全友军豁免（`!isAlliedTo(e)`），避免打中主人其他宠物或召唤物。
  - **悬停与盘旋移动 AI（`NewHoverBeamGoal`）稳定性重构**：
    - 消除原先每刻（每秒20次）全随机选点导致的急停、急转弯与剧烈空中“抽搐”抖动，改用平稳盘旋方位角步进与寻路冷却机制。
    - 移动与盘旋过程中持续平滑锁定目标视线，为施法提供极其稳定的空对地射击平台。
- **AI 目标选择器自建解耦**：
  - 弃用并移除了对 `io.redspace.ironsspellbooks.entity.mobs.goals.*` 的直接依赖，防止因 ISS（Iron's Spells 'n Spellbooks）跨版本变更内部类及构造签名（如 `OwnerGetter` 与 `Supplier<Entity>` 的 ABI 不兼容）导致游戏在注册或实例化时抛出 `NoClassDefFoundError` 崩溃。
  - 新增独立的 AI 目标选择器：
    - `QueenOwnerHurtByTargetGoal`（主人受击反击）
    - `QueenOwnerHurtTargetGoal`（协同攻击主人目标）
    - `QueenCopyOwnerTargetGoal`（跟随生物主人的目标）
    - `QueenHurtByTargetGoal`（受击反击并支持呼叫同伴，同时忽略主人与盟友伤害）
  - 保留并完善了坐下状态校验、友军/召唤物防内讧逻辑及伤害源过滤。
