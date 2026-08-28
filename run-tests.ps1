# =====================================================================
# 单元测试运行脚本
# 封装目的：绕开 PowerShell 对 -Dxxx=yyy 的命名参数解析坑
# （PowerShell 会把 =false 切成独立 token，mvn 报 Unknown lifecycle phase）
#
# 用法：
#   .\run-tests.ps1                                   # 跑全量 *Test 单元测试
#   .\run-tests.ps1 AuthControllerTest                # 跑单个测试类
#   .\run-tests.ps1 "AuthControllerTest+UserControllerTest"  # 跑多个（用 + 分隔）
# =====================================================================

param(
    [string]$TestPattern = ""
)

# 不能用 $ErrorActionPreference = "Stop"
# 原因：mvn / Java 把 SLF4J、logback 日志输出到 stderr，
# PowerShell 会把 stderr 当成错误流，触发 Stop 提前终止脚本。
$ErrorActionPreference = "Continue"

$mvnArgs = @("test", "-Dsurefire.useFile=false")
if ($TestPattern -ne "") {
    $mvnArgs += "-Dtest=$TestPattern"
    $mvnArgs += "-DfailIfNoTests=false"
}

Write-Host "==> mvn $($mvnArgs -join ' ')" -ForegroundColor Cyan

# 关键：用 Start-Process 调用 mvn.cmd，把 stdout/stderr 都写到文件
# 避免 PowerShell 把 mvn 的 stderr (SLF4J 日志) 当成错误流处理
# 也避免 2>&1 让 PowerShell 把 stderr 转成 ErrorRecord 干扰 mvn 进程
$logFile = [System.IO.Path]::GetTempFileName()
$proc = Start-Process -FilePath "mvn.cmd" `
    -ArgumentList $mvnArgs `
    -NoNewWindow `
    -Wait `
    -PassThru `
    -RedirectStandardOutput $logFile `
    -RedirectStandardError "$logFile.err"

Get-Content $logFile, "$logFile.err" 2>$null | Out-Host
Remove-Item $logFile, "$logFile.err" -ErrorAction SilentlyContinue

$exitCode = $proc.ExitCode

if ($exitCode -eq 0) {
    Write-Host "==> BUILD SUCCESS" -ForegroundColor Green
} else {
    Write-Host "==> BUILD FAILURE (exit $exitCode)" -ForegroundColor Red
    # 输出失败用例摘要
    $reports = Get-ChildItem target\surefire-reports\*.txt -ErrorAction SilentlyContinue
    foreach ($r in $reports) {
        $lines = Get-Content $r.FullName | Select-String "FAILURE|ERROR" | Select-Object -First 3
        if ($lines) {
            Write-Host "---- $($r.BaseName) ----" -ForegroundColor Yellow
            $lines | ForEach-Object { Write-Host $_.Line }
        }
    }
}
exit $exitCode
