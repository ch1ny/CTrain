<template>
	<div>
		<el-container>
			<el-main>
				<el-card class="cardBox">
					<el-result icon="success" title="订单处理成功" :subTitle="`${countdown}秒后回到我的订单，如页面未跳转请点击下方按钮`">
						<template slot="extra">
							<el-button type="primary" size="medium" :disabled="countdown >= 0">返回</el-button>
						</template>
					</el-result>
				</el-card>
			</el-main>
		</el-container>
	</div>
</template>

<script>
export default {
	data() {
		return {
			countdown: 3,
		};
	},
	created() {
		let interval = setInterval(() => {
			this.countdown--;
			if (this.countdown < 0) {
				this.$router.push({ path: "/indent" });
				clearInterval(interval);
			}
		}, 1000);
	},
};
</script>

<style scoped>
.cardBox {
	position: absolute;
	left: 25vw;
	top: 10vh;
	width: 50vw;
}
</style>