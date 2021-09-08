<template>
	<div>
		<el-container>
			<el-main>
				<el-card>
					<div slot="header" style="text-align: left">
						<span><b>后台管理</b></span>
					</div>
					<el-tabs v-model="tabPage">
						<el-tab-pane label="列车管理" name="first">
							<Trains></Trains>
						</el-tab-pane>
						<el-tab-pane label="用户管理" name="second">
							<Users></Users>
						</el-tab-pane>
					</el-tabs>
				</el-card>
			</el-main>
		</el-container>
	</div>
</template>

<script>
import Trains from "../../components/Admin/Trains.vue";
import Users from "../../components/Admin/Users.vue";

export default {
	components: {
		Trains,
		Users,
	},
	data() {
		return {
			tabPage: "first",
		};
	},
	created() {
		if (localStorage.getItem("token") == null) {
			this.$router.replace("/");
		}
	},
};
</script>