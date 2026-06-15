import SwiftUI
import Shared

struct ChatSidebar: View {
    let gpuEnabled: Bool
    let onToggleGpu: (Bool) -> Void

    var body: some View {
        NavigationStack {
            List {
                Section("設定") {
                    Toggle("GPU 加速", isOn: Binding(
                        get: { gpuEnabled },
                        set: { onToggleGpu($0) }
                    ))
                }
                Section("履歴") {
                    Text("履歴はまだありません")
                        .foregroundStyle(.secondary)
                        .font(.callout)
                }
            }
            .navigationTitle("メニュー")
            .navigationBarTitleDisplayMode(.inline)
        }
    }
}
