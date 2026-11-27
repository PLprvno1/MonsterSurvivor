# Invoke the existing android Run configuration in the open project. No standalone build/export/install.
Add-Type @'
using System;
using System.Collections.Generic;
using System.Text;
using System.Runtime.InteropServices;
public static class Task05Studio {
 public delegate bool Callback(IntPtr h,IntPtr l);
 [DllImport("user32.dll")] public static extern bool EnumWindows(Callback c,IntPtr l);
 [DllImport("user32.dll")] public static extern uint GetWindowThreadProcessId(IntPtr h,out uint p);
 [DllImport("user32.dll",CharSet=CharSet.Unicode)] public static extern int GetWindowText(IntPtr h,StringBuilder s,int n);
 [DllImport("user32.dll")] public static extern bool IsWindowVisible(IntPtr h);
 [DllImport("user32.dll")] public static extern bool SetForegroundWindow(IntPtr h);
 [DllImport("user32.dll")] public static extern bool ShowWindow(IntPtr h,int mode);
 [DllImport("user32.dll")] public static extern IntPtr GetForegroundWindow();
 [DllImport("user32.dll")] public static extern bool SetProcessDPIAware();
 [StructLayout(LayoutKind.Sequential)] public struct Rect {public int Left,Top,Right,Bottom;}
 [DllImport("user32.dll")] public static extern bool GetWindowRect(IntPtr h,out Rect r);
 [DllImport("user32.dll")] public static extern bool SetCursorPos(int x,int y);
 [DllImport("user32.dll")] public static extern void mouse_event(uint flags,uint dx,uint dy,uint data,UIntPtr extra);
 public static void ClickRun(IntPtr h) {Rect r;GetWindowRect(h,out r);SetCursorPos(r.Left+942,r.Top+26);mouse_event(2,0,0,0,UIntPtr.Zero);mouse_event(4,0,0,0,UIntPtr.Zero);}
 [DllImport("kernel32.dll")] public static extern uint GetCurrentThreadId();
 [DllImport("user32.dll")] public static extern bool AttachThreadInput(uint a,uint b,bool attach);
 [DllImport("user32.dll")] public static extern bool BringWindowToTop(IntPtr h);
 public static void FocusProject(IntPtr h) {
  uint pid;uint foreground=GetWindowThreadProcessId(GetForegroundWindow(),out pid),self=GetCurrentThreadId();
  bool attached=AttachThreadInput(self,foreground,true);
  try {ShowWindow(h,9);BringWindowToTop(h);SetForegroundWindow(h);}finally {if(attached)AttachThreadInput(self,foreground,false);}
 }
 public static IntPtr Find(uint pid) {
  IntPtr result=IntPtr.Zero;
  EnumWindows((h,l)=>{uint p;GetWindowThreadProcessId(h,out p);var s=new StringBuilder(512);GetWindowText(h,s,512);
   if(p==pid && IsWindowVisible(h) && s.ToString().StartsWith("MonsterSurvivor"))result=h;return true;},IntPtr.Zero);
  return result;
 }
}
'@
$taskStudioProcess=Get-Process studio64 -ErrorAction Stop
[Task05Studio]::SetProcessDPIAware() | Out-Null
$taskWindow=[Task05Studio]::Find($taskStudioProcess.Id)
if($taskWindow -eq [IntPtr]::Zero){throw 'MonsterSurvivor project window not found; no shortcut sent'}
$taskShell=New-Object -ComObject WScript.Shell
$taskShell.AppActivate($taskStudioProcess.Id) | Out-Null
[Task05Studio]::ShowWindow($taskWindow,9) | Out-Null
[Task05Studio]::FocusProject($taskWindow)
Start-Sleep -Milliseconds 300
[Task05Studio]::FocusProject($taskWindow)
Start-Sleep -Milliseconds 300
if([Task05Studio]::GetForegroundWindow() -ne $taskWindow){$taskTitle=New-Object System.Text.StringBuilder 512;[Task05Studio]::GetWindowText([Task05Studio]::GetForegroundWindow(),$taskTitle,512) | Out-Null;throw ('Project window was not activated; no shortcut sent. Current foreground: '+$taskTitle.ToString()+' target='+$taskWindow)}
# Position verified from the current project's toolbar (Pixel 4 / android). Fail above
# if the intended project window cannot be selected; this helper is local to this layout.
$taskShell.SendKeys('{ESC}')
Start-Sleep -Milliseconds 150
$taskShell.SendKeys('+{F10}')
$taskMessage=[DateTime]::UtcNow.ToString('o')+' Invoked Android Studio Run (Shift+F10), existing android configuration, project MonsterSurvivor.'
Add-Content -LiteralPath 'D:/Android/MonsterSurvivor/core/build/reports/task05/studio-run.log' -Value $taskMessage -Encoding UTF8
Write-Output $taskMessage
