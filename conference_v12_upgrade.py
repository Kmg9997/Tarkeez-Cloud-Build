from pathlib import Path

p = Path("ConferenceMicPro/app/src/main/java/com/khalid/conferencemic")
main = p / "MainActivity.java"
s = main.read_text()
def replace(old, new):
    global s
    if old not in s: raise RuntimeError("Missing MainActivity pattern: " + old[:80])
    s = s.replace(old, new)
replace('"مكبر صوت بلوتوث — ميكروفون الهاتف",', '"بلوتوث عالي الجودة — ميكروفون الهاتف (الأنقى)",')
replace('"سماعة بلوتوث — ميكروفون السماعة (مكالمات)",', '"بلوتوث المكالمات — ميكروفون السماعة (جودة منخفضة)",')
replace('prefs = getSharedPreferences("mic_settings", MODE_PRIVATE);\n        buildUI();', '''prefs = getSharedPreferences("mic_settings", MODE_PRIVATE);
        if (!prefs.getBoolean("v12_profile_migrated", false)) {
            SharedPreferences.Editor e = prefs.edit().putBoolean("v12_profile_migrated", true);
            if ("bt_call".equals(prefs.getString("route", "auto"))) e.putString("route", "bt_media");
            e.apply();
        }
        buildUI();''')
replace('"صوت مباشر واضح · إعداد لتقليل التأخير · تحكم كامل"', '"بلوتوث أنقى عبر ميكروفون الهاتف · السلكي أسرع"')
replace('if (pos == 3) statusDetail.setText("ميكروفون البلوتوث يستخدم وضع المكالمات وقد تنخفض الجودة.");',
'''if (pos == 3) statusDetail.setText("تنبيه: الميكروفون في سماعة البلوتوث يستخدم وضع مكالمات بجودة محدودة.");
                if (pos == 2) statusDetail.setText("جودة الوسائط: ميكروفون الهاتف ثم سماعة البلوتوث؛ قد يستمر تأخير النقل اللاسلكي.");''')
replace('"في وضع مكبر البلوتوث يُستخدم ميكروفون الهاتف؛ وفي وضع المكالمات يُستخدم ميكروفون السماعة إن دعمه الجهاز."',
'"لجودة أفضل اختر بلوتوث عالي الجودة (ميكروفون الهاتف). ميكروفون السماعة يعني جودة المكالمات. للمزامنة الأسرع استخدم سماعة سلكية."')
replace('String[] labels = {"قاعة مؤتمرات", "أقل صدى", "صدى خفيف", "قاعة كبيرة"};',
'String[] labels = {"صوت مباشر نقي", "قاعة مؤتمرات", "صدى خفيف", "قاعة كبيرة"};')
replace('int[][] vals = {{110, 17, 24}, {100, 0, 0}, {108, 24, 13}, {110, 43, 54}};',
'int[][] vals = {{100, 0, 0}, {110, 17, 24}, {108, 24, 13}, {110, 43, 54}};')
replace('"يقلل مخزن إخراج الصوت عند دعم الهاتف. إذا سمعت تقطيعًا عطّل الوضع. لا يلغي تأخر بلوتوث الوسائط."',
'"يقلل مخزن الصوت عند دعم الجهاز بما فيه وضع بلوتوث الوسائط؛ لا يزيل تأخير النقل اللاسلكي."')
main.write_text(s)

service = p / "MicrophoneService.java"
s = service.read_text()
def replace_service(old, new):
    global s
    if old not in s: raise RuntimeError("Missing service pattern: " + old[:80])
    s=s.replace(old, new)
replace_service('boolean fastMode = lowLatency && !call && !"bt_media".equals(selected);',
'''boolean bluetoothMedia = "bt_media".equals(selected);
            boolean fastMode = lowLatency && !call && !bluetoothMedia;''')
replace_service('''if (fastMode) {
                int targetFrames = Math.max(240, sampleRate / 50); // target ~20 ms''',
'''if (fastMode || (bluetoothMedia && lowLatency)) {
                // ~20 ms app-side wired buffer or ~40 ms app-side A2DP buffer.
                // Radio/codec latency is NOT included.
                int targetFrames = fastMode ? Math.max(240, sampleRate / 50)
                        : Math.max(512, sampleRate / 25);''')
replace_service('if (fastMode && now - lastUpdate > 170) {',
'if ((fastMode || (bluetoothMedia && lowLatency)) && now - lastUpdate > 170) {')
replace_service('if ("bt_media".equals(selected)) modeInfo += " · البلوتوث قد يتأخر";',
'if (bluetoothMedia) modeInfo += " · ميكروفون الهاتف / جودة الوسائط · تأخير بلوتوث متوقع";')
replace_service('case "bt_media": label = "مكبر بلوتوث · ميكروفون الهاتف"; break;',
'case "bt_media": label = "بلوتوث عالي الجودة · ميكروفون الهاتف"; break;')
replace_service('case "bt_call": label = "سماعة بلوتوث · وضع المكالمات"; break;',
'case "bt_call": label = "بلوتوث مكالمات · صوت منخفض الجودة"; break;')
service.write_text(s)
build = Path("ConferenceMicPro/app/build.gradle")
s=build.read_text().replace("versionCode 2", "versionCode 3").replace("versionName '1.1.0'", "versionName '1.2.0'")
build.write_text(s)
