import SwiftUI
import Shared

struct PromptBubble: View {
    let prompt: String
    let collapsed: Bool
    let onTap: () -> Void

    var body: some View {
        Button(action: onTap) {
            HStack(alignment: .bottom, spacing: 6) {
                Text(prompt)
                    .font(.body)
                    .foregroundStyle(.white)
                    .lineLimit(collapsed ? 3 : nil)
                    .multilineTextAlignment(.leading)
                Image(systemName: collapsed ? "chevron.down" : "chevron.up")
                    .font(.system(size: 12, weight: .semibold))
                    .foregroundStyle(.white.opacity(0.7))
            }
            .padding(.horizontal, 14)
            .padding(.vertical, 10)
            .background(
                RoundedRectangle(cornerRadius: 16)
                    .fill(Color(red: 0.25, green: 0.25, blue: 0.25))
                    .overlay(
                        RoundedRectangle(cornerRadius: 16)
                            .strokeBorder(Color.white.opacity(0.18), lineWidth: 1)
                    )
            )
            .frame(maxWidth: 280, alignment: .trailing)
        }
        .buttonStyle(.plain)
    }
}
