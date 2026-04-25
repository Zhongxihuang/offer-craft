# 2-Minute Demo Script

## Opening

This project is an **AI Career Decision & Interview Prep Agent** built in Java with Spring Boot and LangChain4j.  
The key idea is that it does not stop at generic chat. It runs a workflow-first analysis, then uses support chat to continue the conversation around the saved workflow artifact.

## Problem Framing

Most AI career tools can answer questions, but they do not guide a user through an actual decision workflow.  
Here the product does four things in sequence:

1. parse the job description
2. analyze the candidate profile
3. identify the most important fit gaps
4. generate interview prep and next steps

## Canonical Scenario

The demo scenario is a **Senior AI Product Manager** role for an enterprise GenAI platform company.  
The candidate is strong in B2B SaaS product execution, analytics, and experimentation, but weaker in direct LLM platform depth, governance, and technical AI storytelling.

## Workflow Walkthrough

First, I paste the canonical JD and candidate profile into the workflow intake form.  
When I submit, the system returns a structured artifact bundle instead of a freeform chat answer.

At the top, you can see the decision summary: this candidate is **competitive with gaps**.  
Below that, the app breaks the reasoning into JD analysis, candidate analysis, gap analysis, and interview prep.

That structure is important because it makes the agent explainable. In an AI PM interview, I can point to the typed stages and show that this is an orchestrated workflow, not just one long prompt.

## Why The Support Chat Matters

Then I jump into **Support Chat**.  
The chat is linked to the saved `workflowId`, so the follow-up is grounded in the workflow artifact.  
Instead of starting over, I can ask things like:

- why the top gap matters
- how to turn the gaps into a 7-day prep plan
- how to reframe the resume for this role

That creates a clean product loop: **analyze first, then explain and coach**.

## Close

So the portfolio story is not “I built a chatbot.”  
It is: **I built a workflow-first AI agent that produces structured career decision artifacts and then uses linked follow-up chat to deepen the analysis.**
