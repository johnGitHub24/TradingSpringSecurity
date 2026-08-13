# CodeGraphic image export

Source: `docs/codeGraphic.html`  
Tool: `@mermaid-js/mermaid-cli@11`（dark）  
Hook（相對本專案根目錄）：`..\EngineeringOS\eos-minimal\hooks\export-codeGraphic-images.ps1`

| File | Tab |
|------|-----|
| `01-filter.svg` / `.png` | Filter |
| `02-jwt.svg` / `.png` | JWT |
| `03-api.svg` / `.png` | API |
| `04-packages.svg` / `.png` | 套件 |

從**本專案根目錄**重跑：

```powershell
& "..\EngineeringOS\eos-minimal\hooks\export-codeGraphic-images.ps1" -ProjectRoot .
```

勿使用本機絕對路徑或舊 workspace 路徑。
