

# 🍳 CookingPlugin - 마인크래프트 요리 플러그인

직업 플러그인의 커스텀 작물 드롭과 연동되는 **정밀 요리 시스템**입니다.  
전용 조리 도구, 정량 조리, 성공률, 시간 기반 조리 등을 지원하며, GUI 기반으로 직관적인 플레이 경험을 제공합니다.

---

## 🧩 주요 기능

| 기능                       | 설명 |
|--------------------------|------|
| 🎨 **전용 GUI 요리 인터페이스** | 절구, 냄비, 오븐, 팬, 믹서기 등 다양한 조리 도구 |
| 🥕 **직업 플러그인 연동**       | 농부 직업에서 획득한 특수 작물만으로 조리 가능 |
| 🧪 **정량 조리 방식**         | 레시피마다 재료의 정확한 개수 요구 (ex: 코코아가루 2 + 달걀노른자 1) |
| ⏳ **시간 기반 조리 시스템**   | 각 요리마다 조리 시간 존재. ActionBar로 진행 상황 출력 |
| ❌ **조리 중 ESC 누르면 실패 처리** | 중도 취소 시 실패 사운드 및 일부 재료 소멸 |
| 📦 **레시피 YAML로 커스텀 가능** | `recipes.yml`을 통해 레시피 자유 설정 가능 |
| 🎯 **성공률 시스템**          | 요리마다 성공 확률 설정 가능 |
| 🧃 **결과 아이템 커스터마이징**  | 이름, 설명, 재료 수량, 결과물 모두 설정 가능 |

---

## 🔧 설치 방법

1. `CookingPlugin.jar` 플러그인을 서버 `/plugins` 폴더에 넣습니다.
2. `JobPlugin`이 선행 설치되어 있어야 합니다. (`special_crop` ID 연동에 사용)
3. 서버를 재시작하거나 `/reload` 합니다.
4. `/cook` 명령어로 요리 GUI를 테스트합니다.

---

## 📁 플러그인 구조


```sh
CookingPlugin/
├── config.yml (미사용)
├── recipes.yml    # 요리 레시피 정의
├── storage/       # 향후 확장 저장공간
```


---

## 🧾 `recipes.yml` 예시

```yaml
recipes:
  egg_chocolate:
    station: POT
    inputs:
      cocoa_powder: 2
      egg_yolk: 1
    result:
      material: CAKE
      name: §d에그 초콜릿
      lore: §7고급 디저트 요리
    success: 0.7
    time: 6
````

* `station`: 어떤 도구에서 조리 가능한지 (MORTAR, POT, PAN, OVEN, BLENDER)
* `inputs`: 각 재료 ID 및 필요한 개수 (`special_crop` 기반)
* `result`: 결과 아이템 설정 (이름, 설명, 재료 등)
* `success`: 성공 확률 (0.0 \~ 1.0)
* `time`: 조리 시간 (초 단위)

---

## 🚀 명령어

| 명령어     | 설명        | 권한 노드       |
| ------- | --------- | ----------- |
| `/cook` | 요리 GUI 오픈 | cooking.use |

---

## 📌 연동 정보

* **직업 플러그인의 special\_crop 태그**를 기반으로 재료를 인식합니다.
* 각 아이템은 PDC (`special_crop`)에 고유 ID를 포함해야 하며, 이 ID는 `recipes.yml`의 `inputs:`와 일치해야 합니다.

---

## 🛠 향후 계획

* ✅ 다단계 조리 지원 (ex. 재료 → 중간재료 → 요리)
* ✅ 재료 부족 시 경고 메시지
* 🔜 요리 경험치 / 농부 직업 레벨 연동
* 🔜 요리 결과물 낱개 확률 보너스
* 🔜 GUI 꾸미기 및 레시피 북

---

## 🧑‍💻 개발자 노트

* **Spigot 1.16+** 이상 버전 지원
* API 연동을 위해 내부적으로 `PersistentDataContainer` 사용
* 외부 API 연동을 원한다면 `CookingAPI.java`에 확장 가능성 있음

---

## 📣 기여 및 버그 제보

* Issue 또는 PR 환영!
* 직접 서버에 도입한 후 문제가 있다면 스크린샷 및 `recipes.yml` 함께 첨부해주세요.

---

© 2025 PluginCraft 개발
Made with ☕ and 🍫


