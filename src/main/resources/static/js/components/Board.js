import AITipBox from './AITipBox';

const Board = ({ boardId }) => {
    return (
        <Box sx={{ p: 3 }}>
            <Grid container spacing={3}>
                <Grid item xs={12}>
                    <AITipBox type="board" entityId={boardId} />
                </Grid>
                {/* ... existing board content ... */}
            </Grid>
        </Box>
    );
};

export default Board; 