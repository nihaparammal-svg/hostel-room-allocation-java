Write-Host "========================================================" -ForegroundColor Cyan
Write-Host "Starting College Hostel Management System (Spring Boot)" -ForegroundColor Cyan
Write-Host "========================================================" -ForegroundColor Cyan

$env:JAVA_HOME = "C:\Program Files\Eclipse Adoptium\jdk-21.0.12.101-hotspot"
$env:Path = "$env:JAVA_HOME\bin;" + $env:Path

.\mvnw.cmd spring-boot:run
