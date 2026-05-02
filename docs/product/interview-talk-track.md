# 2 分钟面试讲述

## 1. 问题

我观察到很多投 AI 产品或 GenAI 岗位的候选人会把 JD 和简历丢给 ChatGPT，但真正的问题不是“让 AI 给建议”，而是缺少一个稳定决策流程：这份岗位该不该投，最影响结果的差距是什么，接下来几天该怎么准备。

## 2. 需求拆解

我把用户任务拆成五步：解析 JD、提炼 must-haves、分析候选人证据、排序 Top 3 gaps、生成 3-7 天准备计划。这样用户拿到的不是一段聊天，而是一份可以继续推进的决策产物。

## 3. Workflow 设计

后端用 Java、Spring Boot 和 LangChain4j 做四阶段 workflow：JD parsing、candidate analysis、gap analysis、interview prep。前端默认进入 workflow 表单，support chat 被降为结果后的追问和 refine 工作区。

## 4. AI 可信度

我特别强化了可信度表达：每个结果会展示 apply verdict、confidence、decision drivers、missing evidence，以及 Top 3 gaps 为什么会影响招聘判断。工具或搜索不可用时不会让主流程失败，而是显示 fallback notes。

## 5. 验证

我准备了固定 eval cases：强简历、弱简历、不完整 JD、不完整简历、中文 JD、搜索失败和 restore forbidden。不是只看接口是否返回，而是人工检查输出是否具体、可信、可执行。

## 6. 下一步

下一阶段我会把 support chat 进一步升级成 agent loop：用户补充新经历后，系统能生成新的 workflow version，并解释版本变化。长期方向是从单次分析工具升级成 candidate workspace，支持多个目标岗位的决策比较。
