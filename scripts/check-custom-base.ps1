param(
  [string]$ApkPath = "unpackage/debug/android_debug.apk"
)

$ErrorActionPreference = "Stop"

if (!(Test-Path $ApkPath)) {
  Write-Host "未找到 APK：$ApkPath"
  Write-Host "请先在 HBuilderX 执行：运行 → 运行到手机或模拟器 → 制作自定义调试基座（Android）"
  exit 2
}

Write-Host "APK 信息："
Get-Item $ApkPath | Select-Object FullName,Length,LastWriteTime | Format-List
try {
  $hash = Get-FileHash -Algorithm SHA256 -Path $ApkPath
  Write-Host "APK SHA256：$($hash.Hash)"
} catch {
  Write-Host "无法计算 APK hash：$($_.Exception.Message)"
}

python -c @"
import zipfile, re, sys
apk = r'''$ApkPath'''
z = zipfile.ZipFile(apk)

needles = [
  b'CB-KeepAlive',
  b'KeepAliveModule',
  b'io/dcloud/feature/keepalive',
]

matched_entries = [n for n in z.namelist() if re.search(r'CB-KeepAlive|keepalive', n, re.I)]
print('APK entries matched:', len(matched_entries))
for n in matched_entries[:50]:
  print(' -', n)

dexes = [n for n in z.namelist() if n.endswith('.dex')]
print('DEX files:', dexes)

hit_any = False
for dex in dexes:
  data = z.read(dex)
  hit = any(s in data for s in needles)
  print(dex, 'contains_keepalive_strings?', hit, 'size', len(data))
  hit_any = hit_any or hit

if hit_any:
  print('OK: 自定义基座已包含 keepalive 相关代码（至少包含字符串特征）。')
  sys.exit(0)
else:
  print('NG: 自定义基座未发现 keepalive 相关代码。说明插件没有被打进基座。')
  sys.exit(1)
"@


