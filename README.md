# ShtormbrakerMod

Официальная документация мода **ShtormbrakerMod** для Minecraft Forge.  
**English version is provided below.**

---

## Русская версия

### 1. Обзор
**ShtormbrakerMod** добавляет в игру легендарное оружие **Штормбрейкер** с расширенным набором боевых и мобильных возможностей:
- управляемый полет;
- дальний бросок и автоматический возврат;
- вызов молнии по направлению взгляда;
- запуск и остановка грозы;
- постоянные бонусы характеристик при наличии предмета в инвентаре.

Мод ориентирован на динамичный PvE/PvP-геймплей и рассчитан на использование в одиночной игре и на серверах.

### 2. Техническая информация и совместимость

| Параметр | Значение |
|---|---|
| Название мода | `ShtormbrakerMod` |
| Версия мода | `1.0.0` |
| Minecraft | `1.20.1` |
| Forge | `47.2.0` |
| Loader (javafml) | `47+` |
| Лицензия | `MIT` |

### 3. Установка
1. Убедитесь, что используется Minecraft `1.20.1` и Forge `47.2.0` или совместимая версия ветки `47.x`.
2. Собранный файл мода находится по пути: `build\libs\shtormbraker-1.0.0.jar`.
3. Скопируйте `.jar` мода в папку `mods`.
4. Запустите игру через профиль Forge.

### 4. Управление

| Действие | Клавиша / ввод | Описание |
|---|---|---|
| Полет | `ПКМ` (по Штормбрейкеру) | Включение/выключение полета |
| Бросок/возврат | Удержание `ЛКМ` | После короткого удержания выполняется бросок; при возврате предмет возвращается владельцу |
| Удар молнией | `N` | Вызывает молнию в точку по направлению взгляда |
| Гроза | `G` | Переключает состояние грозы (запуск/остановка) |

### 5. Функционал
- **Полет на Штормбрейкере:** быстрое перемещение по направлению взгляда с возможностью оперативного отключения.
- **Бросок Штормбрейкера:** оружие летит к цели, наносит высокий урон, разрушает блоки по траектории и автоматически возвращается.
- **Молния по цели:** точечный удар молнии на большой дистанции.
- **Управление погодой:** запуск или остановка грозы в измерениях с небом и погодой.
- **Пассивные бонусы в инвентаре:** усиление игрока действует, пока Штормбрейкер находится в инвентаре.

### 6. Характеристики

#### 6.1 Параметры предмета
- Прочность: `4096`
- Скорость добычи (tier speed): `12.0`
- Бонус tier к урону атаки: `+20.0`
- Базовый модификатор урона предмета: `+30.0`
- Скорость атаки: `-2.8`
- Редкость: `EPIC`
- Огнестойкость: включена

#### 6.2 Боевые и специальные параметры
- Урон при попадании в броске: `1000.0`
- Скорость полета оружия вперед: `3.4`
- Максимальная дальность броска: `100.0`
- Скорость возврата: `2.6`
- Скорость полета игрока: `2.35`
- Дальность удара молнией: `300.0`
- Интервал визуального следа молнии: `3` тика

#### 6.3 Бонусы игроку при наличии Штормбрейкера в инвентаре
- Максимальное здоровье: `x2.0`
- Скорость передвижения: `x1.5`
- Сила прыжка: `x1.2`
- Регенерация: усиленный эффект (базовая длительность `80` тиков)

### 7. Крафт Штормбрейкера

**Ингредиенты:**
- `2 x minecraft:netherite_ingot`
- `2 x minecraft:blaze_rod`

**Шаблон в верстаке (3x3):**

```text
 N 
NB 
  B
```

Где:
- `N` = `minecraft:netherite_ingot`
- `B` = `minecraft:blaze_rod`

**Скриншот крафта из игры:**

![Крафт Штормбрейкера](<Снимок экрана 2026-05-12 221917.png>)

### 8. Контакты
Разработано **Triad Studio**  
VK: https://vk.com/triadstudio  
Telegram: https://t.me/T3riadStudio

---

## English Version

### 1. Overview
**ShtormbrakerMod** introduces the legendary **Shtormbraker** weapon with an extended set of combat and mobility features:
- controlled flight;
- long-range throw and automatic return;
- lightning strike at the targeted point;
- thunderstorm start/stop control;
- persistent player stat bonuses while the item is in inventory.

The mod is designed for dynamic PvE/PvP gameplay and is suitable for both singleplayer and multiplayer servers.

### 2. Technical Information and Compatibility

| Parameter | Value |
|---|---|
| Mod name | `ShtormbrakerMod` |
| Mod version | `1.0.0` |
| Minecraft | `1.20.1` |
| Forge | `47.2.0` |
| Loader (javafml) | `47+` |
| License | `MIT` |

### 3. Installation
1. Ensure you are using Minecraft `1.20.1` with Forge `47.2.0` or another compatible build from the `47.x` branch.
2. The built mod file is located at: `build\libs\shtormbraker-1.0.0.jar`.
3. Copy the mod `.jar` file into your `mods` folder.
4. Launch the game using the Forge profile.

### 4. Controls

| Action | Key / input | Description |
|---|---|---|
| Flight | `Right Mouse Button` (while using Shtormbraker) | Toggles flight on/off |
| Throw/Recall | Hold `Left Mouse Button` | After a short hold, the weapon is thrown; it then returns to the owner |
| Strike Lightning | `N` | Calls a lightning strike at the point along the player's view direction |
| Thunderstorm | `G` | Toggles thunderstorm state (start/stop) |

### 5. Features
- **Shtormbraker Flight:** high-speed movement in the look direction with instant toggle off.
- **Shtormbraker Throw:** the weapon travels to the target, deals high damage, breaks blocks along its path, and returns automatically.
- **Targeted Lightning:** precise long-range lightning strike.
- **Weather Control:** starts or stops thunderstorm in dimensions that support skylight and weather.
- **Passive Inventory Buffs:** player enhancements remain active while Shtormbraker is present in inventory.

### 6. Characteristics

#### 6.1 Item Parameters
- Durability: `4096`
- Mining speed (tier speed): `12.0`
- Tier attack damage bonus: `+20.0`
- Base item attack damage modifier: `+30.0`
- Attack speed: `-2.8`
- Rarity: `EPIC`
- Fire resistance: enabled

#### 6.2 Combat and Special Parameters
- Throw hit damage: `1000.0`
- Outbound weapon speed: `3.4`
- Maximum throw distance: `100.0`
- Return speed: `2.6`
- Player flight speed: `2.35`
- Lightning strike range: `300.0`
- Lightning trail visual interval: `3` ticks

#### 6.3 Player Buffs While Shtormbraker Is in Inventory
- Max health: `x2.0`
- Movement speed: `x1.5`
- Jump power: `x1.2`
- Regeneration: enhanced effect (base duration `80` ticks)

### 7. Shtormbraker Crafting

**Ingredients:**
- `2 x minecraft:netherite_ingot`
- `2 x minecraft:blaze_rod`

**Crafting table pattern (3x3):**

```text
 N 
NB 
  B
```

Where:
- `N` = `minecraft:netherite_ingot`
- `B` = `minecraft:blaze_rod`

**In-game crafting screenshot:**

![Shtormbraker crafting](<Снимок экрана 2026-05-12 221917.png>)

### 8. Contacts
Developed by **Triad Studio**  
VK: https://vk.com/triadstudio  
Telegram: https://t.me/T3riadStudio
