import SwiftUI
import Shared

struct TurnView: View {
    let turn: ChatTurn
    let showDivider: Bool
    let onTogglePromptCollapse: () -> Void

    var body: some View {
        VStack(alignment: .leading, spacing: 12) {
            HStack {
                Spacer(minLength: 0)
                PromptBubble(
                    prompt: turn.prompt,
                    collapsed: turn.isPromptCollapsed,
                    onTap: onTogglePromptCollapse
                )
            }
            responseView
            if showDivider {
                Divider()
                    .background(Color.white.opacity(0.2))
                    .padding(.vertical, 4)
            }
        }
    }

    @ViewBuilder
    private var responseView: some View {
        switch turn.response {
        case let g as ResponseState.Generating:
            VStack(alignment: .leading, spacing: 8) {
                if !g.partial.isEmpty {
                    Text(g.partial)
                        .font(.body)
                        .foregroundStyle(.white)
                        .frame(maxWidth: .infinity, alignment: .leading)
                }
                ProgressView()
                    .progressViewStyle(.circular)
                    .tint(.white.opacity(0.7))
            }
        case let c as ResponseState.Completed:
            Text(c.text)
                .font(.body)
                .foregroundStyle(.white)
                .frame(maxWidth: .infinity, alignment: .leading)
        case let f as ResponseState.Failed:
            Text("応答エラー: \(f.message)")
                .font(.body)
                .foregroundStyle(.red)
                .frame(maxWidth: .infinity, alignment: .leading)
        default:
            EmptyView()
        }
    }
}
