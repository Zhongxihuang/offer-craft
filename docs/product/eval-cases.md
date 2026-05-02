# Outcome Eval Cases

这组 eval case 用于验证项目是否真的像“职业决策 Agent”，而不是只要接口返回就算成功。评分为人工评估，关注输出是否具体、可信、可执行。

| Case | 输入特征 | 期望输出 | 人工评分 | 主要观察 |
|---|---|---|---:|---|
| Strong Resume | 标准 AI PM JD + 强 enterprise SaaS / AI workflow / metrics / governance 证据 | 应给出 `APPLY_NOW` 或 `APPLY_WITH_REFRAMING`，Top gaps 较少且偏表达优化 | 8/10 | 能识别强证据，准备建议聚焦差异化表达 |
| Weak Resume | 标准 AI PM JD + 泛 PM/运营背景，AI/平台/技术证据弱 | 应降低 fit，Top 3 gaps 指向 AI 实践、平台理解、技术表达 | 8/10 | 差距排序比泛聊天更清楚 |
| Incomplete JD | JD 只写“负责 AI 产品”，缺少场景、指标、技术要求 | 应降低 JD confidence，并提出澄清问题 | 7/10 | 如果输出仍然编造 must-haves，则扣分 |
| Incomplete Resume | 简历只有标题和少量职责，缺少范围、结果、数据 | 应明确 missing evidence，不应虚构影响力 | 8/10 | 需要把“缺证据”转成补充问题 |
| Chinese JD | 中文 AI 产品经理 JD + 中文候选人背景 | UI、错误和自由文本应为自然中文 | 7/10 | 重点检查是否有翻译腔和乱码 |
| Search Failure | `includeCompanyResearch=true` 但 search/RAG 不可用 | workflow 不应失败，应显示 fallback notes | 8/10 | demo mode 不应宣称使用了真实外部搜索 |
| Upload TXT/MD | JD 粘贴 + 简历 MD 上传 | 文件优先，结果可恢复 | 8/10 | 验证上传、持久化、restore |
| Restore Forbidden | token protection enabled, missing token | 返回 403，前端清理恢复状态并提示重新运行或配置 token | 9/10 | 防止无限恢复失败循环 |

## Rubric

- Requirement specificity：是否提炼出真实岗位要求。
- Candidate understanding：是否基于候选人证据，而不是泛泛夸奖。
- Gap usefulness：Top gaps 是否能影响投递或面试结果。
- Prep usefulness：行动计划是否能在 3-7 天内执行。
- Hallucination resistance：输入稀疏时是否承认不确定。
- Graceful degradation：工具失败时是否可用且说明清楚。
