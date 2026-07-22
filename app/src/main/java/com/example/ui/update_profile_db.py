import re

app_db_path = r'c:\Users\v23\Desktop\sınav-merkezi-724\app\src\main\java\com\example\data\AppDatabase.kt'
with open(app_db_path, 'r', encoding='utf-8') as f:
    app_db = f.read()

# Update UserProfile
new_user_profile = """@Entity(tableName = "user_profile")
data class UserProfile(
    @PrimaryKey val id: Int = 1,
    val username: String,
    val identifier: String, // email, phone, or username
    val passwordHash: String, // Mock password
    val targetExam: String, // "YKS (TYT-AYT)", "LGS", "KPSS", "DGS"
    val field: String,      // "Sayısal", "Sözel", "Eşit Ağırlık", "Genel"
    val phone: String = "",
    val address: String = "",
    val email: String = "",
    val birthDate: String = "",
    val createdTimestamp: Long = System.currentTimeMillis()
)"""

app_db = re.sub(r'@Entity\(tableName = "user_profile"\)\ndata class UserProfile\([\s\S]*?createdTimestamp: Long = System\.currentTimeMillis\(\)\n\)', new_user_profile, app_db)
app_db = app_db.replace('version = 1,', 'version = 2,')

with open(app_db_path, 'w', encoding='utf-8') as f:
    f.write(app_db)

# Update ExamViewModel
viewmodel_path = r'c:\Users\v23\Desktop\sınav-merkezi-724\app\src\main\java\com\example\ui\ExamViewModel.kt'
with open(viewmodel_path, 'r', encoding='utf-8') as f:
    viewmodel = f.read()

# Replace updateExamPreferences
new_update_prefs = """    fun updateExamPreferences(targetExam: String, field: String, phone: String = "", address: String = "", email: String = "", birthDate: String = "", name: String = "") {
        viewModelScope.launch {
            _profile.value?.let { p ->
                val updated = p.copy(
                    targetExam = targetExam, 
                    field = field,
                    phone = phone,
                    address = address,
                    email = email,
                    birthDate = birthDate,
                    username = if (name.isNotBlank()) name else p.username
                )
                repository.updateProfile(updated)
                _profile.value = updated
            }
        }
    }"""

viewmodel = re.sub(r'    fun updateExamPreferences.*?_profile\.value = updated\n            }\n        }\n    }', new_update_prefs, viewmodel, flags=re.DOTALL)
with open(viewmodel_path, 'w', encoding='utf-8') as f:
    f.write(viewmodel)

print("ViewModel and Database updated")
