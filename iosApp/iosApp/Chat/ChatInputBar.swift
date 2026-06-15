import SwiftUI
import Shared

struct ChatInputBar: View {
    @Binding var input: String
    let enabled: Bool
    let isSendVisible: Bool
    let onSend: () -> Void

    var body: some View {
        HStack(alignment: .center, spacing: 8) {
            TextField(
                "",
                text: $input,
                prompt: Text(enabled ? "メッセージを入力" : "モデル準備中...")
                    .foregroundStyle(.white.opacity(0.5)),
                axis: .vertical
            )
            .lineLimit(1...4)
            .textFieldStyle(.plain)
            .foregroundStyle(.white)
            .disabled(!enabled)
            .padding(.vertical, 12)

            if isSendVisible {
                Button(action: onSend) {
                    Image(systemName: "paperplane.fill")
                        .font(.system(size: 18))
                        .foregroundStyle(.white)
                        .frame(width: 36, height: 36)
                }
            }
        }
        .padding(.horizontal, 16)
        .background(
            RoundedRectangle(cornerRadius: 28)
                .fill(Color(red: 0.25, green: 0.25, blue: 0.25))
        )
        .padding(.horizontal, 16)
        .padding(.vertical, 12)
    }
}
