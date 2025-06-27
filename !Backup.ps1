# Prompt for commit message
$commitMessage = Read-Host "Enter commit message"

# Perform Git backup
git add .
git commit -m "$commitMessage"
$commitHash = git rev-parse HEAD
git push origin master

# Get current date in MM_dd_yyyy format
$date = Get-Date -Format "MM_dd_yyyy"

# Copy project to folder named with date and commit hash
$source = "C:\Code\Android\BorrowBuddy"
$destination = "G:\My Drive\Programming\Android\BorrowBuddy - $date - $commitHash"
$destination = "C:\Code\Android\BorrowBuddy - $date - $commitHash"
Copy-Item -Path $source -Destination $destination -Recurse -Force

Write-Host "Backup to GitHub and local folder ($destination) complete!"
Write-Host "Commit hash: $commitHash"
Write-Host "View this commit at: https://github.com/Flyersfan1972/BorrowBuddy/commit/$commitHash"
Write-Host "Press Enter to exit."
Read-Host