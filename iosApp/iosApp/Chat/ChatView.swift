import SwiftUI
import Shared

struct ChatView: View {
    @State private var bridge = ChatViewModelBridge()
    @State private var showSidebar = false
    @State private var inputDraft: String = ""

    var body: some View {
        VStack(spacing: 0) {
            AppHeader(
                state: bridge.state,
                onMenuTapped: { showSidebar = true },
                onLogoTapped: { bridge.dispatch(IntentMainLogoTapped.shared) },
                onNewChatTapped: {
                    bridge.dispatch(IntentMainStartNewChat.shared)
                    inputDraft = ""
                }
            )
            Divider()
                .background(Color.white.opacity(0.3))
            ChatBody(
                bridge: bridge,
                inputDraft: $inputDraft
            )
        }
        .background(Color(red: 0.17, green: 0.17, blue: 0.17).ignoresSafeArea())
        .preferredColorScheme(.dark)
        .sheet(isPresented: $showSidebar) {
            ChatSidebar(
                gpuEnabled: bridge.state.gpuEnabled,
                onToggleGpu: { bridge.dispatch(IntentSettingToggleGpu(enabled: $0)) }
            )
            .presentationDetents([.medium, .large])
        }
        .alert(
            "エラー",
            isPresented: Binding(
                get: { bridge.pendingError != nil },
                set: { if !$0 { bridge.clearError() } }
            ),
            actions: { Button("OK") { bridge.clearError() } },
            message: { Text(bridge.pendingError ?? "") }
        )
        .onChange(of: inputDraft) { _, new in
            bridge.dispatch(IntentMainUpdateInput(text: new))
        }
        .onChange(of: bridge.state.input) { _, new in
            if new != inputDraft { inputDraft = new }
        }
    }
}

private struct ChatBody: View {
    let bridge: ChatViewModelBridge
    @Binding var inputDraft: String

    var body: some View {
        VStack(spacing: 0) {
            ScrollViewReader { proxy in
                ScrollView {
                    LazyVStack(alignment: .leading, spacing: 8) {
                        ForEach(Array(bridge.state.turns.enumerated()), id: \.element.id) { index, turn in
                            TurnView(
                                turn: turn,
                                showDivider: index < bridge.state.turns.count - 1,
                                onTogglePromptCollapse: {
                                    bridge.dispatch(IntentMainTogglePromptCollapse(turnId: turn.id))
                                }
                            )
                            .id(turn.id)
                        }
                    }
                    .padding(.horizontal, 16)
                    .padding(.vertical, 8)
                }
                .mask(
                    LinearGradient(
                        stops: [
                            .init(color: .black, location: 0),
                            .init(color: .black, location: 0.88),
                            .init(color: .clear, location: 1),
                        ],
                        startPoint: .top,
                        endPoint: .bottom
                    )
                )
                .onTapGesture {
                    UIApplication.shared.sendAction(
                        #selector(UIResponder.resignFirstResponder),
                        to: nil, from: nil, for: nil
                    )
                }
                .onChange(of: bridge.state.turns.count) { _, _ in
                    if let last = bridge.state.turns.last {
                        withAnimation { proxy.scrollTo(last.id, anchor: .bottom) }
                    }
                }
                .onChange(of: lastPartial) { _, _ in
                    if let last = bridge.state.turns.last {
                        withAnimation(.linear(duration: 0.1)) {
                            proxy.scrollTo(last.id, anchor: .bottom)
                        }
                    }
                }
            }

            ChatInputBar(
                input: $inputDraft,
                enabled: bridge.state.isInputEnabled,
                isSendVisible: bridge.state.isSendVisible,
                onSend: { bridge.dispatch(IntentMainSend.shared) }
            )
        }
    }

    private var lastPartial: String {
        guard let last = bridge.state.turns.last else { return "" }
        if let g = last.response as? ResponseStateGenerating { return g.partial }
        return ""
    }
}
