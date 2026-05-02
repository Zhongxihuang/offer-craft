$ErrorActionPreference = "Stop"

$repoRoot = Split-Path -Parent $PSScriptRoot
$frontendRoot = Join-Path $repoRoot "frontend"
$requestPath = Join-Path $repoRoot "docs\demo\ai-pm-canonical\workflow-request.json"
$stdoutLogPath = Join-Path $repoRoot "target\demo-smoke-backend.out.log"
$stderrLogPath = Join-Path $repoRoot "target\demo-smoke-backend.err.log"

if (-not (Test-Path $requestPath)) {
    throw "Canonical workflow request not found: $requestPath"
}

if ($env:JAVA_HOME) {
    $env:Path = (Join-Path $env:JAVA_HOME "bin") + ";" + $env:Path
}

Push-Location $repoRoot
try {
    & .\mvnw.cmd test

    Push-Location $frontendRoot
    try {
        npm run build
    }
    finally {
        Pop-Location
    }

    $env:CAREER_AI_MODE = "demo"

    foreach ($path in @($stdoutLogPath, $stderrLogPath)) {
        if (Test-Path $path) {
            Remove-Item $path -Force
        }
    }

    $process = Start-Process -FilePath ".\mvnw.cmd" `
        -ArgumentList "spring-boot:run" `
        -WorkingDirectory $repoRoot `
        -PassThru `
        -RedirectStandardOutput $stdoutLogPath `
        -RedirectStandardError $stderrLogPath

    try {
        $ready = $false
        for ($i = 0; $i -lt 60; $i++) {
            Start-Sleep -Seconds 2
            try {
                Invoke-WebRequest -Uri "http://localhost:8081/api/career/workflow/health-check-smoke" -Method Get -UseBasicParsing | Out-Null
            }
            catch {
                $statusCode = $_.Exception.Response.StatusCode.value__
                if ($statusCode -eq 404) {
                    $ready = $true
                    break
                }
            }
        }

        if (-not $ready) {
            throw "Backend did not become ready in demo mode. See $stdoutLogPath and $stderrLogPath"
        }

        $requestBody = Get-Content -Path $requestPath -Raw
        $workflow = Invoke-RestMethod -Uri "http://localhost:8081/api/career/workflow/analyze" `
            -Method Post `
            -ContentType "application/json" `
            -Body $requestBody

        if (-not $workflow.workflowId) {
            throw "Workflow analyze response did not contain workflowId."
        }

        Invoke-RestMethod -Uri ("http://localhost:8081/api/career/workflow/" + $workflow.workflowId) -Method Get | Out-Null

        $chatUrl = "http://localhost:8081/api/ai/chat?memoryId=501&workflowId=$($workflow.workflowId)&message=Turn%20my%20top%20gaps%20into%20a%207-day%20prep%20plan"
        $chatResponse = Invoke-WebRequest -Uri $chatUrl -Method Get -UseBasicParsing
        if ($chatResponse.Content -notmatch "data:") {
            throw "Workflow-linked support chat did not return SSE chunks."
        }

        Write-Host "Demo smoke passed."
        Write-Host "Workflow ID: $($workflow.workflowId)"
    }
    finally {
        if ($process -and -not $process.HasExited) {
            Stop-Process -Id $process.Id -Force
        }
    }
}
finally {
    Pop-Location
}
