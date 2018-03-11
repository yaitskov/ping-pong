export function fullDraftMatch(id) {
    return {
        id: id || 1,
        state: 'Draft',
        level: 1
    };
}
