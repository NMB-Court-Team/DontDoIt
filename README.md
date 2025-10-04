# 不要做挑战 (English version follows)

这是一个Paper插件，它提供了一个名为《不要做挑战》的游戏的功能。

## 命令

**插件所有命令的入口都在`/criteria`命令中。**

想了解命令更详细的功能，你可以直接阅读源代码，命令类是`net.astrorbits.dontdoit.system.CriteriaCommand`。

> 注：以下命令中，标上`*`符号的代表所有玩家都可以执行，未标上`*`符号的代表仅管理员可以执行。

### `/criteria reset`

重置游戏。会重置世界边界和队伍血量，但不会重置地图。

### `/criteria settings`

游戏设置相关的命令，有两个子命令：

- `/criteria settings set`: 打开游戏设置界面。
- `/criteria settings reset`: 重置游戏设置。

### *`/criteria trigger <team:TeamId>`

触发指定队伍的自定义词条。

### *`/criteria guess <player:PlayerName> <guessed:Boolean>`

标记指定玩家猜对了词条或猜错了词条。

### `/criteria trigger-forced <team:TeamId>`

强制触发指定队伍的词条，不论是不是自定义词条。

### `/criteria change <team:TeamId>`

强制切换指定队伍的词条（不会重置自动切换词条倒计时）。

### `/criteria grant <team:TeamId> <criteria:CriteriaName>`

强制将指定队伍的词条换成指定的词条（不会重置自动切换词条倒计时）。

### `/criteria get <team:TeamId>`

获取指定队伍的信息，目前只有一个子命令：

- `/criteria get <team:TeamId> criteria`: 获取指定队伍当前的词条。

### `/criteria set-life <team:TeamId> <life:Integer>`

强制将指定队伍的血量设置为指定值。

- 如果队伍仍存活，将队伍血量设置为0，则会导致队伍淘汰
- 如果队伍已淘汰，将队伍血量设置为大于0的值，则会导致队伍复活

## 游戏玩法

嘻嘻，readme还没写

你可以直接搜视频，这样了解玩法更简单。

## 配置

### config/game_settings.yml
包含:
- 文字显示配置
- 队伍物品配置
- 矿物生成配置
- 游戏玩法相关的配置项

一般不需要对此文件进行更改，除非你需要定制矿物生成。

### config/criteria.yml

包含大部分词条，有部分词条是由插件动态生成的，可以在设置里进行开关，不能增加。

增加词条需要注意保持格式严格一致，如`:`，`-`后需要加空格，缩进一致等。

注意事项：

- 大部分词条可以用`reversed: true`表示反向的词条，不同词条反向的含义也不同，请查看对应词条的`reversed`字段的注释。
- 物品、方块相关的词条可以用`*`表示任意物品、方块，用`#`表示物品、方块标签，如`#leaves`，`#pickaxes`。
- 如果需要判断多个物品，直接用`,`分隔写多个id，其中任意一个id符合条件就会触发。
- 实体相关的词条可以用`*`表示任意实体，用`/`表示无来源。无来源也包含在任意实体里。
- `damage_type`和`effect`也可以用`*`表示任意。
- 范围相关的词条用`..`表示，如`3..`表示x≥3，`5..6`表示5≤x≤6。
- 详细解释见`criteria.yml`内注释。

## 支持的语言

- 中文
- 英文

要添加其他语言的支持，请创建一个新的pull request。把对应语言的文件放到`languages\<你使用的语言>\`这个文件夹里。

如果要把游戏语言换成你使用的语言，你需要把你使用的语言对应的文件`criteria.yml`和`game_settings.yml`放到`<你的服务器>/plugins/DontDoIt/`这个文件夹下，然后重启服务器。

-------

# Don't Do It

A paper plugin that provides a minigame called **Don't Do It**.

## Commands

**All the command entry point of the plugin is in the command `/criteria`.**

To know the detailed functions of the commands, you can just read the source code directly. The command class is `net.astrorbits.dontdoit.system.CriteriaCommand`.

> Note: Commands that mark with `*` symbol are executable for all the players. And commands that **NOT** mark with `*` symbol are executable for only operators.

### `/criteria reset`

Reset the game. It resets the world border and the life count of all the teams, but does not reset the game area.

### `/criteria settings`

Game settings related commands. Containing two subcommands:

- `/criteria settings set`: Open the game settings GUI.
- `/criteria settings reset`: Reset the game settings.

### *`/criteria trigger <team:TeamId>`

Trigger the custom criteria of the specified team.

### *`/criteria guess <player:PlayerName> <guessed:Boolean>`

Mark the specified player guessing criteria as successful or failed.

### `/criteria trigger-forced <team:TeamId>`

Forcibly trigger the criteria of the specified team, regardless of whether the criteria is custom or not.

### `/criteria change <team:TeamId>`

Forcibly change the criteria of the specified team (not reset the countdown of auto-changing criteria).

### `/criteria grant <team:TeamId> <criteria:CriteriaName>`

Forcibly change the criteria of the specified team to the specified criteria (not reset the countdown of auto-changing criteria).

### `/criteria get <team:TeamId>`

Get information of the specified team. Containing only one subcommand currently:

- `/criteria get <team:TeamId> criteria`: Get current criteria of the specified team.

### `/criteria set-life <team:TeamId> <life:Integer>`

Forcibly set the life count of the specified team to the specified value.

- If the team is still alive, setting the life count of the team to 0 leads to eliminate the team.
- If the team is already eliminated, setting the life count of the team to the value above 0 leads to revive the team.

## How to Play

readme WIP

You can just search for videos so that you can easily know how to play.

## Config

### config/game_settings.yml

Includes:

- Game texts configuration
- Team selection items configuration
- Ore gen configuration
- In-game undynamic settings

Editing this file is usually unnecessary, unless you want to change the ore gen settings.

### config/criteria.yml

Some of the criteria is dynamically modified by plugin so you can only switch them on/off at ingame settings.

If you want to add more criteria, please maintain the exact formatting.

Note:
- Some of the criteria support adding `reversed: true` to indicate the reversed version. Different criteria have different meanings when reversed. See the comments of corresponding `reversed` field.
- Items or blocks related criteria supports using `*` to indicate any item or block. Use `#` to indicate item tags or block tags, e.g. `#leaves`, `#pickaxes`.
- Use `,` to split items, which represents any of the item will trigger the criteria.
- Use `*` to represent any entity. Use `/` repersent no entity (or no source), which is also included in any entity symbol `*`.
- Field `damage_type` and `effect` also support using `*` to indicate any of the type.
- Use `..` to represent number range. For example, `3..` means x≥3, `5..6` means 5≤x≤6
- For more detailed info, see the comments in `criteria.yml`.


## Supported Languages

- Chinese
- English

You can add more languages support by creating a new pull request. Please put the file into `languages\<your-language>\`.

To switch the game language to your language, you should put file `criteria.yml` and `game_settings.yml` of your language into 
directory `<your-server>/plugins/DontDoIt/`, then restart the server.


